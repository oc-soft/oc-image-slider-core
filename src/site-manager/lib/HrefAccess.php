<?php

global $lib_dir;
require_once implode('/', [$lib_dir, 'Db.php']);
require_once implode('/', [$lib_dir, 'MgrConfig.php']);


/**
 * manage hyper reference access
 */
class HrefAccess {

    /**
     * href access
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
     * insert href
     */
    function insert_href($href, $query_selector) {
        $db_commands = MgrConfig::$instance->get_db_commands();

        $db_name = $db_commands['db-name'];
        $prefix = $db_commands['prefix'];
        
        $insert_href = $db_commands['insert-href']['query'];
        $insert_href = implode('', $insert_href);
        $insert_href = sprintf($insert_href, $db_name, $prefix);

        $find_href_id = $db_commands['find-href-id']['query'];
        $find_href_id = implode('', $find_href_id);
        $find_href_id = sprintf($find_href_id, $db_name, $prefix);

        $update_selector = $db_commands['update-selector']['query'];
        $update_selector = implode('', $update_selector);
        $update_selector = sprintf($update_selector, $db_name, $prefix); 


        $stmt = $this->get_client()->stmt_init();

        $stmt->prepare($insert_href);

        if ($stmt->errno == 0) {
            $stmt->bind_param('s', $href);
        }
        if ($stmt->errno == 0) {
            $stmt->execute();
        }
        
        if ($stmt->errno == 0) {
            $stmt->prepare($find_href_id);
        }
        if ($stmt->errno == 0) {
            $stmt->bind_param('s', $href);
        }
        if ($stmt->errno == 0) {
            $b_state = $stmt->execute();
            if ($b_state) {
                $b_state = $stmt->bind_result($id_ref);
            }
            if ($b_state) {
                $b_state = $stmt->fetch();
            }
            if ($b_state) {
                $href_id = $id_ref;
            }
        }

        if ($stmt->errno == 0 && isset($href_id)) {
            $stmt->prepare($update_selector);
        }
        if ($stmt->errno == 0 && isset($href_id)) {
            $stmt->bind_param('iss', $href_id,
                $query_selector,
                $query_selector);
        }
        if ($stmt->errno == 0) {
            $stmt->execute();
        }
        if ($stmt->errno == 0) {
            $result = $href_id;
        }
        $stmt->close();
        return $result;
    }

    /**
     * find href id
     */
    function find_href_id($href) {
        $db_commands = MgrConfig::$instance->get_db_commands();

        $db_name = $db_commands['db-name'];
        $prefix = $db_commands['prefix'];
        
 
        $find_href_id = $db_commands['find-href-id']['query'];
        $find_href_id = implode('', $find_href_id);
        $find_href_id = sprintf($find_href_id, $db_name, $prefix);

        $stmt = $this->get_client()->stmt_init();

        $stmt->prepare($find_href_id);
        if ($stmt->errno == 0) {
            $stmt->bind_param('s', $href);
        }
        if ($stmt->errno == 0) {
            $b_state = $stmt->execute();
            if ($b_state) {
                $b_state = $stmt->bind_result($id_ref);
            }
            if ($b_state) {
                $b_state = $stmt->fetch();
            }
            if ($b_state) {
                $result = $id_ref;
            }
        }

        $stmt->close();
        return $result;
    }

    /**
     * find query selector
     */
    function find_selector($href) {
        $db_commands = MgrConfig::$instance->get_db_commands();

        $db_name = $db_commands['db-name'];
        $prefix = $db_commands['prefix'];
        
 
        $find_href_id = $db_commands['find-href-id']['query'];
        $find_href_id = implode('', $find_href_id);
        $find_href_id = sprintf($find_href_id, $db_name, $prefix);

        $find_selector = $db_commands['find-selector']['query'];
        $find_selector = implode('', $find_selector);
        $find_selector = sprintf($find_selector, $db_name, $prefix);


        $stmt = $this->get_client()->stmt_init();

        $stmt->prepare($find_href_id);
        if ($stmt->errno == 0) {
            $stmt->bind_param('s', $href);
        }
        if ($stmt->errno == 0) {
            $b_state = $stmt->execute();
            if ($b_state) {
                $b_state = $stmt->bind_result($id_ref);
            }
            if ($b_state) {
                $b_state = $stmt->fetch();
            }
        }
        if ($stmt->errno == 0 && isset($id_ref)) {
            $stmt->prepare($find_selector);
        }
        if ($stmt->errno == 0 && isset($id_ref)) {
            $stmt->bind_param('i', $id_ref);
        }

        if ($stmt->errno == 0) {
            $b_state = $stmt->execute();
            if ($b_state) {
                $b_state = $stmt->bind_result($result);
            }
            if ($b_state) {
                $stmt->fetch();
            }
        }

        $stmt->close();
        return $result;
    }

    /**
     * list up access
     */
    function list_access($begin_time, $end_time) {
        $db_commands = MgrConfig::$instance->get_db_commands();

        $db_name = $db_commands['db-name'];
        $prefix = $db_commands['prefix'];
        
 
        $list_access = $db_commands['list-access-1']['query'];
        $list_access = implode('', $list_access);
        $list_access = sprintf($list_access, $db_name, $prefix);

        $stmt = $this->get_client()->stmt_init();

        $stmt->prepare($list_access);
        if ($stmt->errno == 0) {
            $stmt->bind_param('ss', $begin_time, $end_time);
        }
        if ($stmt->errno == 0) {
            $b_state = $stmt->execute();
            if ($b_state) {
                $b_state = $stmt->bind_result(
                    $src_href, $dest_href, $access);
            }
            $result = [];
            if ($b_state) {
                while ($stmt->fetch()) {
                    $result[] = [$src_href, $dest_href, $access];
                }
            }
        } else {
            error_log($stmt->error);
        }

        $stmt->close();
        return $result;
    }

    /**
     *  list href table 
     */
    function list_href(
        $narrow_down,
        $order_by) {

        $db_commands = MgrConfig::$instance->get_db_commands();

        $db_name = $db_commands['db-name'];
        $prefix = $db_commands['prefix'];
        
        $list_href = $db_commands['list-href']['query'];
        $list_href = implode('', $list_href);
        $list_href = sprintf($list_href, $db_name, $prefix);

        if ($narrow_down) {
            $narrow_downs = [];
            foreach ($narrow_down as $entry) {
                $narrow_downs[] = sprintf("%s LIKE '%s'", 
                    $entry['column'], $entry['pattern']);
            }
            $where_clauses = sprintf(
                'WHERE %s', implode(' AND ', $narrow_downs));

        } else {
            $where_clauses = '';
        }

        if ($order_by) {
            $order_by_clauses_elems = [];
            foreach ($order_by as $entry) {
                if (strcasecmp($entry['sort'], 'DESC') == 0) {
                    $order_by_clauses_elems[] = sprintf('%s DESC',
                        $entry['column']);
                } else {
                    $order_by_clauses_elems[] = sprintf('%s ASC', 
                        $entry['column']);
                }
            }
            $order_by_clauses = sprintf(
                'ORDER BY %s',
                implode(',', $order_by_clauses_elems));
        } else {
            $order_by_clauses = '';
        }

        $query = implode(' ',
            [$list_href, $where_clauses, $order_by_clauses]);

        $query_res = $this->get_client()->query($query);

        if ($query_res) {
            $result = [];
            while (TRUE) {
                $row = $query_res->fetch_array(MYSQLI_NUM);
                if ($row) {
                    $result[] = $row;
                } else {
                    break;
                }
            }
        }
        return $result;
    }
    /**
     *  list href selector
     */
    function list_href_selector(
        $narrow_down,
        $order_by) {

        $db_commands = MgrConfig::$instance->get_db_commands();

        $db_name = $db_commands['db-name'];
        $prefix = $db_commands['prefix'];
        
        $list_href = $db_commands['list-href-selector']['query'];
        $list_href = implode('', $list_href);
        $list_href = sprintf($list_href, $db_name, $prefix);

        if ($narrow_down) {
            $narrow_downs = [];
            foreach ($narrow_down as $entry) {
                $column = $entry['column'];
                $narrow_downs[] = sprintf("%s LIKE '%s'", 
                    $column, $entry['pattern']);
            }
            $where_clauses = sprintf(
                'AND %s', implode(' AND ', $narrow_downs));

        } else {
            $where_clauses = '';
        }

        if ($order_by) {
            $order_by_clauses_elems = [];
            foreach ($order_by as $entry) {
                if (strcasecmp($entry['sort'], 'DESC') == 0) {
                    $order_by_clauses_elems[] = sprintf('%s DESC',
                        $entry['column']);
                } else {
                    $order_by_clauses_elems[] = sprintf('%s ASC', 
                        $entry['column']);
                }
            }
            $order_by_clauses = sprintf(
                'ORDER BY %s',
                implode(',', $order_by_clauses_elems));
        } else {
            $order_by_clauses = '';
        }

        $query = implode(' ',
            [$list_href, $where_clauses, $order_by_clauses]);

        $query_res = $this->get_client()->query($query);

        if ($query_res) {
            $result = [];
            while (TRUE) {
                $row = $query_res->fetch_array(MYSQLI_NUM);
                if ($row) {
                    $result[] = $row;
                } else {
                    break;
                }
            }
        } else {
            error_log($this->get_client()->error);
        }
        return $result;
    }


    /**
     * handle insert ajax request
     */
    function handle_insert() {
        if (isset($_REQUEST['href'])
            && isset($_REQUEST['selector'])) {
            $href_id = $this->insert_href(
                $_REQUEST['href'], 
                $_REQUEST['selector']);
            if ($href_id) {
                $response = ['href-id' => $href_id];
            } else {
                $response = ['status' => FALSE];
            }
        } else {
            $response = ['status' => FALSE];
        }
        return $response;
    }

    /**
     * handle find href id
     */
    function handle_find_href_id() {
        if (isset($_REQUEST['href'])) {
            $href_id = $this->find_href_id(
                $_REQUEST['href']);
            if ($href_id) {
                $response = ['href-id' => $href_id];
            } else {
                $response = ['status' => FALSE];
            }
        } else {
            $response = ['status' => FALSE];
        }
        return $response;

    }
    /**
     * handle find selector
     */
    function handle_find_selector() {
        if (isset($_REQUEST['href'])) {
            $selector = $this->find_selector(
                $_REQUEST['href']);
            if ($href_id) {
                $response = ['selector' => $selector];
            } else {
                $response = ['status' => FALSE];
            }
        } else {
            $response = ['status' => FALSE];
        }
        return $response;
    }

    /**
     * handle list up access
     */
    function handle_list_access() {
        if (isset($_REQUEST['begin'])
            && isset($_REQUEST['end'])) {
            $access_list = $this->list_access(
                $_REQUEST['begin'], 
                $_REQUEST['end']);
            if (isset($access_list)) {
                $response = ['access-list' => $access_list];
            } else {
                $response = ['status' => FALSE];
            }
        } else {
            $response = ['status' => FALSE];
        }
        return $response;
    }


    /**
     * handle list href 
     */
    function handle_list_href() {
        $narrow_down = NULL;
        $order_by = NULL;
        if (isset($_REQUEST['narrow-down'])) {
            $narrow_down = json_decode($_REQUEST['narrow-down'], TRUE);
        }
        if (isset($_REQUEST['order-by'])) {
            $order_by = json_decode($_REQUEST['order-by'], TRUE);
        }
        $href_list = $this->list_href(
            $narrow_down, $order_by);
        if (isset($href_list)) {
            $response = ['href-list' => $href_list];
        } else {
            $response = ['status' => FALSE];
        }
        return $response;
    }

    /**
     * handle list href and selector
     */
    function handle_list_href_selector() {
        $narrow_down = NULL;
        $order_by = NULL;
        if (isset($_REQUEST['narrow-down'])) {
            $narrow_down = json_decode($_REQUEST['narrow-down'], TRUE);
        }
        if (isset($_REQUEST['order-by'])) {
            $order_by = json_decode($_REQUEST['order-by'], TRUE);
        }
        $href_list_selector = $this->list_href_selector(
            $narrow_down, $order_by);
        if (isset($href_list_selector)) {
            $response = ['href-list-selector' => $href_list_selector];
        } else {
            $response = ['status' => FALSE];
        }
        return $response;
    }



    /**
     * handle ajax request
     */
    function handle_request() {
        if (isset($_REQUEST['insert'])) {
            $result = $this->handle_insert(); 
        } else if (isset($_REQUEST['find-href-id'])) {
            $result = $this->handle_find_href_id();
        } else if (isset($_REQUEST['find-selector'])) {
            $result = $this->handle_find_selector();
        } else if (isset($_REQUEST['list-access'])) {
            $result = $this->handle_list_access();
        } else if (isset($_REQUEST['list-href'])) {
            $result = $this->handle_list_href();
        } else if (isset($_REQUEST['list-href-selector'])) {
            $result = $this->handle_list_href_selector();
        }
        return $result;
    }
}

HrefAccess::$instance = new HrefAccess();


// vi: se ts=4 sw=4 et:
