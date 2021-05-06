<?php
global $lib_dir;

require_once implode('/', [$lib_dir, 'HrefAccess.php']);
require_once implode('/', [$lib_dir, 'MgrConfig.php']);


class Tracker {
    /**
     * traker instance
     */
    static $instance;


    /**
     * constructor
     */
    function __construct() {
    }

    /**
     * get database client
     */
    function get_client() {
        return Db::$instance->get_client();
    }




    /**
     *  insert tracking data  
     */
    function insert_access(
        $src_href_id,
        $dst_href_id) {
        $db_commands = MgrConfig::$instance->get_db_commands();

        $db_name = $db_commands['db-name'];
        $prefix = $db_commands['prefix'];
        
        $insert_access = $db_commands['insert-access']['query'];
        $insert_access = implode('', $insert_access);
        $insert_access = sprintf($insert_access, $db_name, $prefix);

        $stmt = $this->get_client()->stmt_init();

        $stmt->prepare($insert_access);

        if ($stmt->errno == 0) {
            $stmt->bind_param('ii', $dst_href_id, $src_href_id);
        }
        if ($stmt->errno == 0) {
            $stmt->execute();
        }
        if ($stmt->errno != null) {
            error_log($stmt->error);

        }
        $stmt->close();
        return $stmt->errno == 0;
     }


    /**
     * handle insertion 
     */
    function handle_insert_access() {

        if ($_REQUEST['href-dst']) {
            $href_access = HrefAccess::$instance;
            $config = MgrConfig::$instance;
            $commands = $config->get_db_commands();
            if (isset($_REQUEST['href-src'])) {
                $src_href = $_REQUEST['href-src'];
            } else {
                $src_href = $commands['special-href']['somewhere'];
            }
                
            $src_id = $href_access->find_href_id($src_href);
            $dst_id = $href_access->find_href_id($_REQUEST['href-dst']);

            if (isset($src_id) && isset($dst_id)) {
                $result = $this->insert_access($src_id, $dst_id);
            }
        }
        return $result;
    }


    /**
     * handle http request
     */
    function handle_request() {
        if (isset($_REQUEST['insert'])) {
            $state = $this->handle_insert_access();
            
            if (isset($state)) {
                $result = [
                    'status' => $status];
            } 
        }
        return $result;
    }
}


Tracker::$instance = new Tracker();

// vi: se ts=4 sw=4 et:
