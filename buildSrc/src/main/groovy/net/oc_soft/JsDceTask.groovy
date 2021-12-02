package net.oc_soft

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal


import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection


/**
 * javascript dead code elimination
 */
abstract class JsDceTask extends DefaultTask {

    /**
     * destination directory
     */
    @OutputDirectory
    File destinationDirectory

    /**
     * development mode suppress dead code elimination
     */
    @Input
    boolean devMode

    /**
     * input source file
     */
    @InputFile
    File source

    // called at initializing time
    {
        devMode = false
        doLast {
            runDce()
        }
    } 

    /**
     * run dead code ellimination
     */
    void runDce() {
        def res = project.javaexec {
            main = 'org.jetbrains.kotlin.cli.js.dce.K2JSDce'
            classpath = compilerClasspath
            args '-output-dir', destinationDirectory
            if (devMode) {
                args '-dev-mode'
            }
            args source
        }
        res.assertNormalExitValue()
    }

    /**
     * compiler classpath
     */
    @Internal
    FileCollection getCompilerClasspath() {
        def result = project.objects.fileCollection()
        def compilerClassConfigName = 'kotlinCompilerClasspath'
        result.setFrom(
            project.configurations.named(compilerClassConfigName))
        return result 
    }
    
    
}



// vi: se ts=4 sw=4 et:
