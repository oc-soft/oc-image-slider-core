<?php
function resolve_functions($a_file) {
    $parent_dir = dirname($a_file);
  
    $functions_path = implode('/', [$parent_dir, 'mgr-functions.php']);
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
global $root_dir;

require_once implode('/', [$lib_dir, 'I18n.php']);
I18n::$instance->init_locale_for_message(
  implode('/', [$root_dir, 'locale']));


?><!DOCTYPE HTML>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- head contents -->
  <?php echo $html_webpack_head_tags; ?>
</head>
<body>
  <!-- body contents -->
  <?php echo $html_webpack_body_tags; ?>
</body>

</html>
<?php
// vi: se ts=2 sw=2 et:
