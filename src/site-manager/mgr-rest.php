<?php
require_once './mgr-functions.php';

(function () {
    global $lib_dir;

    if (isset($_REQUEST['href-access'])) {
        require_once implode('/', [$lib_dir, 'HrefAccess.php']); 
        $response = HrefAccess::$instance->handle_request();
    } else if (isset($_REQUEST['db'])) {
        require_once implode('/', [$lib_dir, 'Db.php']);
        $response = Db::$instance->handle_request();
    } else if (isset($_REQUEST['track'])) {
        require_once implode('/', [$lib_dir, 'Tracker.php']);
        $response = Tracker::$instance->handle_request();
    }
    if (isset($response)) {
        echo json_encode($response);
    } else {
        http_response_code(404);
    }
})();

// vi: set ts=4 sw=4 et:
