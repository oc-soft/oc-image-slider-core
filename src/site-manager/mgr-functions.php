<?php


require_once 'common-functions.php';
global $lib_dir;
require_once implode('/', [$lib_dir, 'site-mgr-client-resources.php']);
require_once implode('/', [$lib_dir, 'MgrConfig.php']);
require_once implode('/', [$lib_dir, 'ResourceResolve.php']);

global $html_webpack_head_tags;
$html_webpack_head_tags = ResourceResolve::$instance->update_tags(
    $html_webpack_head_tags); 

global $html_webpack_body_tags;

$html_webpack_body_tags = ResourceResolve::$instance->update_tags(
    $html_webpack_body_tags); 



// vi: se ts=4 sw=4 et:
