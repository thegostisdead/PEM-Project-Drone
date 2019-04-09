class i18n {

    static initialize() {
        i18n.translation = {};
        i18n.language = null;

        i18n.DIVS = {
            "SETTINGS_SECTION": document.getElementById("settings-section-i18n")
        }

        i18n.registerHardcodedLanguages();
        i18n.prepareSettingsSection();

        i18n.selectLanguage(LANGUAGE_DEFAULT);
    }

    static registerHardcodedLanguages() {
        let english = i18n.registerLanguage("en", "English");
        english.set("date.at", "at");
        english.set("quality.temperature", "Temperature");
        english.set("quality.pressure", "Pressure");
        english.set("quality.humidity", "Humidity");
        english.set("flight.list.lasted", "Flight duration");
        english.set("flight.list.lasted.running", "Always running");
        english.set("flight.list.lasted.rushed", "Rushed");
        english.set("flight.list.item.others", "Others");
        english.set("gallery.text.no-picture", "No picture.");
        english.set("section.tooltip.statistics", "Statistics");
        english.set("section.tooltip.gallery", "Picture Gallery");
        english.set("section.tooltip.debug", "Debugging");
        english.set("section.tooltip.settings", "Settings");
        english.set("section.modal.settings.header", "Settings (not final)");
        english.set("section.modal.settings.item.language", "Language");
        english.set("section.tooltip.map-lock-toggle", "Toggle Map Controls Locking");

        let french = i18n.registerLanguage("fr", "Français");
        french.set("date.at", "à");
        french.set("quality.temperature", "Température");
        french.set("quality.pressure", "Pression");
        french.set("quality.humidity", "Humidité");
        french.set("flight.list.lasted", "Durée de vol");
        french.set("flight.list.lasted.running", "Toujours en cours");
        french.set("flight.list.lasted.rushed", "Terminé prématurément");
        french.set("flight.list.item.others", "Autres");
        french.set("gallery.text.no-picture", "Aucune image.");
        french.set("section.tooltip.statistics", "Statistiques");
        french.set("section.tooltip.gallery", "Gallerie d'image");
        french.set("section.tooltip.debug", "Débogage");
        french.set("section.tooltip.settings", "Paramètres");
        french.set("section.modal.settings.header", "Paramètres (non final)");
        french.set("section.modal.settings.item.language", "Langage");
        french.set("section.tooltip.map-lock-toggle", "Activer/désactiver le verrouillage des contrôle de la carte");
    }

    static prepareSettingsSection() {
        let html = "";

        html += "<h4 class=\"translatable\" data-i18n=\"section.modal.settings.item.language\">?<h4>"
        
        for (let language in i18n.translation) {
            language = i18n.translation[language];

            let activePart = language.code == LANGUAGE_DEFAULT ? "active " : "";

            html += "<a onclick=\"i18n.selectLanguage('" + language.code + "', this);\" href=\"#\" class=\"" + activePart + "settings-item-language list-group-item list-group-item-action\">";
            html += "	<div class=\"d-flex w-100 justify-content-between\">\n";
            html += "		<h5 class=\"mb-1\">" + language.name + "</h5>\n";
            html += "	</div>\n";
            html += "</a>\n";
        }

        i18n.DIVS.SETTINGS_SECTION.innerHTML = html;
    }

    static registerLanguage(code, name) {
        return i18n.translation[code] = new LanguageMap(code, name);
    }

    static selectLanguage(code, sourceElement) {
        i18n.language = i18n.translation[code];

        let elements = document.getElementsByClassName("translatable");
        for (let element of elements) {
            let dataset = element.dataset;
            let key = dataset["i18n"];

            if (key == null) {
                console.warn("i18n: You are using a \"translatable\" class without adding the \"i18n\" dataset.", element);
            } else {
                element.innerHTML = i18n.get(key);
            }
        }

        if (sourceElement != null) {
            for (let element of document.getElementsByClassName("settings-item-language")) {
                element.classList.remove("active");
            }

            sourceElement.classList.add("active");
        }
    }

    static get(key, defaultValue = null) {
        let value = i18n.language.map[key];

        if (value != null) {
            return value;
        }

        return defaultValue != null ? defaultValue : ("%" + key + "%");
    }

}

class LanguageMap {

    constructor(code, name) {
        this.code = code;
        this.name = name;
        this.map = {};
    }

    set(key, value) {
        this.map[key] = value;
    }

}