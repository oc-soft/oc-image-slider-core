<?php

global $lib_dir;
require_once implode('/', [$lib_dir, 'MgrConfig.php']);

/**
 * data base manage ment
 */
class Db {

    /**
     * database instance
     */
    static $instance;

    /**
     * get database client
     */
    function get_client() {
        if (!isset($this->db_client)) {
            $config = MgrConfig::$instance;
            $db_conf = $config->get_db_conf();
            $this->db_client = new mysqli(
                $db_conf['host'],
                $db_conf['user'],
                $db_conf['password'],
                $db_conf['database'],
                $db_conf['port']);
        }
        return $this->db_client;
    }

    /**
     * create database
     */
    function create() {
        $config = MgrConfig::$instance;
        $commands = $config->get_db_commands();

        $prefix = $commands['prefix'];
        $db_name = $commands['db-name'];
        $create_commands = $commands['create-db'];
        $result = isset($create_commands);
        if ($result) {
            foreach ($create_commands as $command) {
                $query = implode($command);
                $query = sprintf($query, $db_name, $prefix);
                $this->get_client()->query($query);
                $result = $this->get_client()->errno == 0;
                if (!$result) {
                    break;
                }
            }
        }
        $this->init_tables();
        return $result;
    }

    /**
     * initialize tables
     */
    function init_tables() {
        $config = MgrConfig::$instance;
        $commands = $config->get_db_commands();

        $prefix = $commands['prefix'];
        $db_name = $commands['db-name'];

        $init_commands = $commands['init-tables'];
        $stmt = $this->get_client()->stmt_init();
        foreach ($init_commands as $init_command) {
            $command = $init_command["command"];
            $query = $commands[$command]['query'];
            $query = implode('', $query);
            $query = sprintf($query, $db_name, $prefix);
            $stmt->prepare($query); 
            foreach ($init_command['params'] as $param) {
                $stmt->bind_param($init_command['bind-types'], ... $param);
                $stmt->execute();
            }    
            
        }
        $stmt->close();
    }

    /**
     * list tables
     */
    function list_tables() {
        $config = MgrConfig::$instance;
        $commands = $config->get_db_commands();
        $prefix = $commands['prefix'];
        $db_name = $commands['db-name'];
        $list_query = $commands['list-tables']['query'];
        $state = isset($list_query);
        if ($state) {
            $list_query = implode('', $list_query);
            $list_query = sprintf($list_query, $db_name, $prefix);
        }
        if ($state) {
            $query_res = $this->get_client()->query($list_query);
        }
        if (isset($query_res)) {
            $result = [];
                       
            $res = $query_res->fetch_row();
            if ($res) {
                while ($res) {
                    if (count($res) > 0) {
                        $result[] = $res[0];
                    }
                    $res = $query_res->fetch_row();
                    if (!$res) {
                        break;
                    }
                }
            }
            $query_res->close();
        }
        return $result;
    }


    /**
     * handle ajax request
     */
    function handle_request() {
        if (isset($_REQUEST['create'])) {
            $state = $this->create();
            $result = ['state' => $state];
        } else if (isset($_REQUEST['list-tables'])) {
            $state = $this->list_tables();
            $result = ['state' => $state];
        }
        return $result;
    }
}

Db::$instance = new Db();

// vi: se ts=4 sw=4 et:
