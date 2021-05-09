<?php

/**
 * manage site path
 */
class SitePath {

    /**
     * site path object
     */
    static $instance;

    /**
     * get relative functins path from document root
     */
    function get_relative_functions_path_from_document_root() {
        global $root_dir;
        $doc_root = $_SERVER['DOCUMENT_ROOT'];
        if (strpos($root_dir, $doc_root) == 0) {
            $result = substr($root_dir, strlen($doc_root)); 
        } else {
            $result = '';
        }
        return $result;
    }
    /**
     * resolve path 
     */
    function resolve_path($path) {
        $functions_path = 
            $this->get_relative_functions_path_from_document_root();
        $replacement = sprintf('%s${1}', $functions_path);
        $replaced_path = preg_replace('/^\$\{functions-root\}(.*)/',
            $replacement, $path);
        if ($replaced_path) {
            $result = $replaced_path;
        } else {
            $result = $path;
        }
        return $result;
    } 
}

SitePath::$instance = new SitePath();
// vi: se ts=4 sw=4 et:
