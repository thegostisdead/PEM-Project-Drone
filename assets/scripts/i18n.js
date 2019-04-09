class i18n {

    static initialize() {
        i18n.translation = {};
        i18n.language = null;

        let english = i18n.registerLanguage("English");
        english.set("date.at", "at");
        english.set("quality.temperature", "Temperature");
        english.set("quality.pressure", "Pressure");
        english.set("quality.humidity", "Humidity");
        english.set("flight.list.lasted", "Flight duration");
        english.set("flight.list.lasted.running", "Always running");
        english.set("flight.list.lasted.rushed", "Rushed");
        english.set("flight.list.item.others", "Others");
        english.set("gallery.text.no-picture", "No picture.");

        let french = i18n.registerLanguage("Français");
        french.set("date.at", "à");
        french.set("quality.temperature", "Température");
        french.set("quality.pressure", "Pression");
        french.set("quality.humidity", "Humidité");
        french.set("flight.list.lasted", "Durée de vol");
        french.set("flight.list.lasted.running", "Toujours en cours");
        french.set("flight.list.lasted.rushed", "Terminé prématurément");
        french.set("flight.list.item.others", "Autres");
        french.set("gallery.text.no-picture", "Aucune image.");

        //i18n.selectLanguage(french.name);
    }

    static registerLanguage(languageName) {
        if (i18n.language == null) {
            i18n.language = languageName;
        }

        return new LanguageMap(languageName, i18n.translation[languageName] = {});
    }

    static selectLanguage(languageName) {
        i18n.language = languageName;
    }

    static get(key, defaultValue = null) {
        let value = i18n.translation[i18n.language][key];

        if (value != null) {
            return value;
        }

        return defaultValue != null ? defaultValue : ("%" + key + "%");
    }

}

class LanguageMap {

    constructor(name, map) {
        this.name = name;
        this.map = map;
    }

    set(key, value) {
        this.map[key] = value;
    }

    get(key, defaultValue = null) {
        let value = this.map[key];

        if (value != null) {
            return value;
        }

        return defaultValue != null ? defaultValue : ("%" + key + "%");
    }

}