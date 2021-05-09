<?php

global $lib_dir;
require_once implode('/', [$lib_dir, 'SitePath.php']);

/**
 * resolve resource file
 */
class ResourceResolve {

    /**
     * this object 
     */
    static $instance;


    /**
     * resolve image file
     */
    function resolve_image($image_path) {
        global $root_dir;
        
        $path = implode('/', [$root_dir, 'img', $image_path]);

        if (is_file($path)) { 
            $content_type = @mime_content_type($path);
            if ($content_type == FALSE) {
                $content_type = 'application/octetstream';
            }
            header('Content-Description: File Transfer');
            header('Content-Disposition: attachment; filename="'.basename($image_path).'"');
            header('Expires: 0');
            header('Cache-Control: must-revalidate');
            header('Pragma: public');
            header(sprintf('Content-Type: %s', $content_type));
            header('Content-Length: ' . filesize($path));
            readfile($path);
        } else {
            http_response_code(404);
        }
    }

    /**
     * update resource tags
     */
    function update_tags(
        $tags) {
        $site_path = SitePath::$instance;
        $functions_root =
            $site_path->get_relative_functions_path_from_document_root();

        $replacement = sprintf(
            '$1$2%s$3', $functions_root);
        $replaced = preg_replace('/(href|src)(=")(\/[^\/])/', 
            $replacement, $tags); 
        if ($replaced == FALSE) {
            $result = $tags;
        } else {
            $result = $replaced;
        }
        return $result;
    }

    /**
     * handle request
     */
    function handle_request() {
        
        if (isset($_REQUEST['image'])) {
            $this->resolve_image($_REQUEST['image']);
        }
    }
}

ResourceResolve::$instance = new ResourceResolve();

// vi: se ts=4 sw=4 et:
