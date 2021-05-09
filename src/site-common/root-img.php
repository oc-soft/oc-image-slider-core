<?php

function resolve_functions($a_file) {
    $parent_dir = dirname($a_file);
  
    $functions_path = implode('/', [$parent_dir, 'common-functions.php']);
    if (is_file($functions_path)) {
       $result = $functions_path;
    } else {
        if ($parent_dir != $a_file) {
            $result = resolve_functions($parent_dir);
        }
    }
    return $result;
}

require_once resolve_functions(__FILE__);

global $lib_dir;

require_once implode('/', [$lib_dir, 'ResourceResolve.php']);


ResourceResolve::$instance->handle_request();

// vi: se ts=4 sw=4 et:
