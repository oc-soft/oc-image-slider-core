<?php

/**
 * manager configuration
 */
class MgrConfig {

    /**
     * configuaration instance
     */
    static $instance;

    /**
     * constructor
     */
    function __construct() {

    }

    /**
     * get database configuaration
     */
    function get_db_conf() {
        if (!isset($this->db_conf)) {
            global $root_dir;
            $contents = @file_get_contents(
                implode('/', [$root_dir, 'config', 'db.json']));
            if ($contents) {
                $this->db_conf = json_decode($contents, TRUE);
            }
        }
        return $this->db_conf;
    }

    /**
     * get database related commands
     */
    function get_db_commands() {
        if (!isset($this->db_commands)) {
            global $root_dir;
            $contents = @file_get_contents(
                implode('/', [$root_dir, 'config', 'db-commands.json']));

            if ($contents) {
                $this->db_commands = json_decode($contents, TRUE);
            }
        }
        return $this->db_commands;
    }

}

MgrConfig::$instance = new MgrConfig();

// vi: se ts=4 sw=4 et:
