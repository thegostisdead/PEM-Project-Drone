class i18n {

    static initialize() {
        i18n.translation = {};
        i18n.language = null;

        i18n.DIVS = {
            "SETTINGS_SECTION": document.getElementById("settings-section-i18n")
        }

        i18n.COOKIES = {
            "LANGUAGE": {
                name: "language",
                default: LANGUAGE_DEFAULT
            }
        }

        i18n.registerHardcodedLanguages();
        i18n.prepareSettingsSection();
        i18n.restoreLanguageFromCookies();
    }

    static registerHardcodedLanguages() {
        let english = i18n.registerLanguage("en", "English");
        english.set("date.at", "at");
        english.set("socket.error.disconnected", "Socket disconnected.");
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
        english.set("section.tooltip.back-to-flight", "Back to current flight");
        english.set("section.modal.settings.header", "Settings (not final)");
        english.set("section.tooltip.map-lock-toggle", "Toggle Map Controls Locking");
        english.set("gallery.picture.show-position", "Show position");
        english.set("settings.general.title", "General");
        english.set("settings.general.language.title", "Language");
        english.set("settings.maps.title", "Maps");
        english.set("settings.maps.api-key", "Google API Key");
        english.set("settings.maps.view-type", "View Type");
        english.set("settings.maps.view-type.roadmap", "Roadmap");
        english.set("settings.maps.view-type.satellite", "Satellite");
        english.set("settings.maps.view-type.hybrid", "Hybrid");
        english.set("settings.maps.view-type.terrain", "Terrain");
        english.set("settings.x.button.apply", "Apply");
        english.set("history.picture.show", "SHOW");
        english.set("history.picture.move-to", "MOVE TO");
        english.set("gps.latitude", "Latitude");
        english.set("gps.longitude", "Longitude");
        english.set("gps.altitude", "Altitude");
        english.set("gps.value.default", "-");
        english.set("gps.unit.degree", "°");
        english.set("gps.unit.meter", "m");
        english.set("dashboard.message.nothing", "No element to display.");

        let french = i18n.registerLanguage("fr", "Français");
        french.set("date.at", "à");
        french.set("socket.error.disconnected", "Flux de données déconnecté.");
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
        french.set("section.tooltip.back-to-flight", "Retour au vol actuel");
        french.set("section.modal.settings.header", "Paramètres (non final)");
        french.set("section.tooltip.map-lock-toggle", "Activer/désactiver le verrouillage des contrôle de la carte");
        french.set("gallery.picture.show-position", "Afficher la position");
        french.set("settings.general.title", "Générale");
        french.set("settings.general.language.title", "Langage");
        french.set("settings.maps.title", "Carte");
        french.set("settings.maps.api-key", "Clé d'API Google");
        french.set("settings.maps.view-type", "Type de vue");
        french.set("settings.maps.view-type.roadmap", "Route");
        french.set("settings.maps.view-type.satellite", "Satellite");
        french.set("settings.maps.view-type.hybrid", "Hybride");
        french.set("settings.maps.view-type.terrain", "Terrain");
        french.set("settings.x.button.apply", "Appliquer");
        french.set("history.picture.show", "AFFICHER");
        french.set("history.picture.move-to", "ALLER A");
        french.set("gps.latitude", "Latitude");
        french.set("gps.longitude", "Longitude");
        french.set("gps.altitude", "Altitude");
        french.set("gps.value.default", "-");
        french.set("gps.unit.degree", "°");
        french.set("gps.unit.meter", "m");
        french.set("dashboard.message.nothing", "Aucun élément à afficher.");
    }

    static prepareSettingsSection() {
        let html = "";

        html += "<div class=\"row no-gutters border rounded overflow-hidden flex-md-row mb-4 shadow-sm h-md-250 position-relative\" style=\"margin: 8px;\">\n";
        html += "    <div class=\"col p-4 d-flex flex-column position-static\">\n";
        html += "        <strong class=\"d-inline-block mb-2 text-primary translatable\" data-i18n=\"settings.general.language.title\">?</strong>\n";
        html += "        <div id=\"settings-section-i18n\">\n";
        html += "            <h4 class=\"translatable\" data-i18n=\"language.name\">?</h4>\n";
        html += "            <div>\n";

        for (let language in i18n.translation) {
            language = i18n.translation[language];

            let activePart = language.code == LANGUAGE_DEFAULT ? "active " : "";

            html += "                <a onclick=\"i18n.selectLanguage('" + language.code + "', this);\" href=\"#\" class=\"" + activePart + "settings-item-language list-group-item list-group-item-action\">\n";
            html += "                    <div class=\"d-flex w-100 justify-content-between\">\n";
            html += "                        <h5 class=\"mb-1\">" + language.name + "</h5>\n";
            html += "                        <em>" + language.code.toUpperCase() + "</em>\n";
            html += "                    </div>\n";
            html += "                </a>\n";
        }
        
        html += "            </div>\n";
        html += "        </div>\n";
        html += "    </div>\n";
        html += "</div>\n";

        i18n.DIVS.SETTINGS_SECTION.innerHTML = html;
    }

    static restoreLanguageFromCookies() {
        let cookieLanguage = Cookies.get(i18n.COOKIES.LANGUAGE.name);
        let correspondingLanguage = i18n.translation[cookieLanguage];

        if (cookieLanguage == null || correspondingLanguage == null) {
            correspondingLanguage = i18n.translation[i18n.COOKIES.LANGUAGE.default];
        }

        i18n.selectLanguage(correspondingLanguage.code);
    }

    static registerLanguage(code, name) {
        return i18n.translation[code] = new LanguageMap(code, name);
    }

    static selectLanguage(code, sourceElement) {
        Cookies.set(i18n.COOKIES.LANGUAGE.name, code);
        i18n.language = i18n.translation[code];

        i18n.applyOn(document);

        if (sourceElement != null) {
            for (let element of document.getElementsByClassName("settings-item-language")) {
                element.classList.remove("active");
            }

            sourceElement.classList.add("active");
        }
    }

    static applyOn(container) {
        let elements = container.getElementsByClassName("translatable");
        for (let element of elements) {
            let dataset = element.dataset;
            let key = dataset["i18n"];

            if (key == null) {
                console.warn("i18n: You are using a \"translatable\" class without adding the \"i18n\" dataset.", element);
            } else {
                element.innerHTML = i18n.get(key);
            }
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

        this.set("language.name", name);
        this.set("language.name.upper", name.toUpperCase());
        this.set("language.code", code);
        this.set("language.code.upper", code.toUpperCase());
    }

    set(key, value) {
        this.map[key] = value;
    }

}