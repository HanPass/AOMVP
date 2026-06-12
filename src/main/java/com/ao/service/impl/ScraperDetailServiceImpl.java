package com.ao.service.impl;

import com.ao.dto.AppelOffre;
import com.ao.service.ScraperDetailService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ScraperDetailServiceImpl implements ScraperDetailService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final Pattern MONEY_PATTERN = Pattern.compile("(\\d[\\d\\s.,\\u00A0]*\\d|\\d)");

    private static final List<String> DOMAIN_LABELS = List.of(
            "domaine d'activité",
            "domaine activite",
            "domaine",
            "catégorie principale",
            "categorie principale",
            "catégorie",
            "categorie"
    );
    private static final List<String> MARKET_TYPE_LABELS = List.of(
            "type de marché",
            "type de marche",
            "nature de marché",
            "nature de marche"
    );
    private static final List<String> BUDGET_LABELS = List.of(
            "estimation",
            "estimation ttc",
            "estimation en dhs",
            "estimation en dhs ttc",
            "montant estimé",
            "montant estime",
            "budget"
    );

    @Override
    public AppelOffre enrich(AppelOffre ao) {
        try {
            Document doc = Jsoup.connect(ao.getUrlDetail())
                    .userAgent("Mozilla/5.0")
                    .timeout(30_000)
                    .get();

            enrichFromDocument(ao, doc);

        } catch (Exception e) {
            log.warn("Erreur détail AO {}", ao.getReference(), e);
        }
        return ao;
    }

    void enrichFromDocument(AppelOffre ao, Document doc) {
        // Sur la page détail, l'organisme n'est pas toujours présent.
        String organisme = doc.select("div[id$=_panelBlocDenomination]")
                .text()
                .replace("Acheteur public :", "")
                .trim();

        if (!organisme.isBlank()) {
            ao.setOrganisme(organisme);
        }

        String dateText = doc.select("div.cloture-line")
                .text()
                .replace("\u00A0", " ")
                .trim();

        if (ao.getDateLimite() == null) {
            LocalDateTime dateLimite = parseDateLimite(dateText);
            ao.setDateLimite(dateLimite);
        }

        extractValue(doc, DOMAIN_LABELS)
                .filter(this::hasText)
                .ifPresent(ao::setDomaine);
        extractValue(doc, MARKET_TYPE_LABELS)
                .filter(this::hasText)
                .ifPresent(ao::setTypeMarche);
        extractValue(doc, BUDGET_LABELS)
                .flatMap(this::parseBudget)
                .ifPresent(ao::setBudgetEstime);
    }

    private Optional<String> extractValue(Document doc, List<String> labels) {
        Elements candidates = doc.select("td, th, div, span, label, p, li, dt, dd");
        for (Element candidate : candidates) {
            String ownText = clean(candidate.ownText());
            String allText = clean(candidate.text());

            Optional<String> inlineValue = labels.stream()
                    .map(label -> valueAfterInlineLabel(ownText, label))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(this::hasText)
                    .findFirst();
            if (inlineValue.isPresent()) {
                return inlineValue;
            }

            if (!matchesAnyLabel(ownText, labels) && !matchesAnyLabel(allText, labels)) {
                continue;
            }

            Optional<String> siblingValue = valueFromSibling(candidate, labels);
            if (siblingValue.isPresent()) {
                return siblingValue;
            }

            Optional<String> parentValue = valueFromParent(candidate, labels);
            if (parentValue.isPresent()) {
                return parentValue;
            }
        }

        return Optional.empty();
    }

    private Optional<String> valueAfterInlineLabel(String text, String label) {
        String normalizedText = normalize(text);
        String normalizedLabel = normalize(label);
        int index = normalizedText.indexOf(normalizedLabel);
        if (index < 0) {
            return Optional.empty();
        }

        int separator = text.indexOf(':');
        if (separator < 0) {
            return Optional.empty();
        }

        return Optional.of(clean(text.substring(separator + 1)));
    }

    private Optional<String> valueFromSibling(Element candidate, List<String> labels) {
        Element sibling = candidate.nextElementSibling();
        while (sibling != null) {
            String text = clean(sibling.text());
            if (hasText(text) && !matchesAnyLabel(text, labels)) {
                return Optional.of(text);
            }
            sibling = sibling.nextElementSibling();
        }
        return Optional.empty();
    }

    private Optional<String> valueFromParent(Element candidate, List<String> labels) {
        Element parent = candidate.parent();
        if (parent == null) {
            return Optional.empty();
        }

        String text = clean(parent.text());
        for (String label : labels) {
            Optional<String> value = valueAfterInlineLabel(text, label);
            if (value.isPresent()) {
                return value;
            }
        }

        String withoutLabels = text;
        for (String label : labels) {
            withoutLabels = withoutLabels.replaceAll("(?i)" + Pattern.quote(label), "");
        }
        withoutLabels = clean(withoutLabels.replace(":", " "));
        return hasText(withoutLabels) && !matchesAnyLabel(withoutLabels, labels)
                ? Optional.of(withoutLabels)
                : Optional.empty();
    }

    private boolean matchesAnyLabel(String value, List<String> labels) {
        String normalized = normalize(value);
        return labels.stream().map(this::normalize).anyMatch(normalized::contains);
    }

    private Optional<BigDecimal> parseBudget(String value) {
        String text = clean(value)
                .replace("MAD", "")
                .replace("DHS", "")
                .replace("DH", "")
                .replace("TTC", "");
        Matcher matcher = MONEY_PATTERN.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }

        String amount = matcher.group(1)
                .replace("\u00A0", " ")
                .replaceAll("\\s+", "");

        if (amount.contains(",") && amount.contains(".")) {
            amount = amount.lastIndexOf(',') > amount.lastIndexOf('.')
                    ? amount.replace(".", "").replace(",", ".")
                    : amount.replace(",", "");
        } else if (amount.contains(",")) {
            amount = amount.replace(".", "").replace(",", ".");
        } else if (amount.indexOf('.') != amount.lastIndexOf('.')) {
            amount = amount.replace(".", "");
        }

        try {
            return Optional.of(new BigDecimal(amount));
        } catch (NumberFormatException exception) {
            log.debug("Budget non parsable: '{}'", value);
            return Optional.empty();
        }
    }

    private String clean(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\u00A0", " ").replaceAll("\\s+", " ").trim();
    }

    private String normalize(String value) {
        String cleaned = clean(value).toLowerCase(Locale.ROOT);
        return java.text.Normalizer.normalize(cleaned, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private LocalDateTime parseDateLimite(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
        } catch (Exception e) {
            log.warn("Date limite non parsable: '{}'", text);
            return null;
        }
    }
}
