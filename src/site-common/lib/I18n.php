<?php

/**
 * internationalization
 */
class I18n {

    /**
     * internationalization object
     */
    static $instance;

    /**
     * get locale mapping  
     */
    function get_locale_map() {
        if (!isset($this->locale_map)) {
            global $root_dir;

            $contents = @file_get_contents(
                implode('/', [$root_dir, 'config', 'locale-map.json']));
            if ($contents) {
                $this->locale_map = json_decode($contents, TRUE);
            }
        }
        return $this->locale_map;
    }

    /**
     * get prefered language
     */

    function get_preferred_language() {
        
        if (isset($_REQUEST['lang'])) {
            $result = $_REQUEST['lang'];
        } else {
            $result = Locale::acceptFromHttp(
                $_SERVER['HTTP_ACCEPT_LANGUAGE']);
        }
        return $result;
    }


    /**
     * set locale for message
     */
    function init_locale_for_message(
        $locale_dir) {
        $lang = $this->get_preferred_language();

        $locales = $this->locale_to_locales($lang);

        $result = setlocale(LC_MESSAGES, $locales);
        if ($result) {
            putenv(sprintf('LC_MESSAGES=%s', $result));
        } else {
            $saved_locale = getenv('LOCPATH');
            putenv(sprintf('LOCPATH=%s', $locale_dir));

            $result = setlocale(LC_MESSAGES, $locales);
            if ($result) {
                putenv(sprintf('LC_MESSAGES=%s', $result));
            }
            if ($saved_locale) {
                putenv(sprintf('LOCPATH=%s', $saved_locale));
            } else {
                putenv('LOCPATH');
            }
        }
        return $result;
    }


    /**
     *  locale to locales
     */
    function locale_to_locales($locale) {
        $result = [$locale];
        $locale_map = $this->get_locale_map();
        if ($locale_map) {
            if (isset($locale_map[$locale])) {
                $aliases = $locale_map[$locale];
                foreach ($aliases as $alias) {
                    $result[] = $alias;
                }
            }
        }
        return $result;
    }
    


    /**
     * set locale with LOCPATH environement.
     */
    function set_locale($category, $locale, $fallback_locale_dir) {
        if (!isset($fallback_locale_dir)) {
            global $root_dir;
            $fallback_locale_dir = implode('/', $root_dir, 'locale');
        }

        $locales = $this->locale_to_locales($locale);
        $result = setlocale($category, $locales);
        
        if (!$result) {
            putenv(sprintf('LOCPATH=%s', $fallback_locale_dir));
            $result = setlocale($category, $locales);
            putenv('LOCPATH');
        }
        return $result;
    }

}

I18n::$instance = new I18n();
// vi: se ts=4 sw=4 et:
