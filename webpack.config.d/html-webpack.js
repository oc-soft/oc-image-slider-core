

/**
 * handle html webpack
 */
class HtmlWebpack {

  /**
   * constructor
   */
  constructor() {
  }


  /**
   * setup webpack configuration
   */
  setupWebpack(config) {
    this.setupHtmlPlugin(config)
    this.setupCdnPlugin(config)
  }


  /**
   *  create style exclude chunks 
   */
  createStyleExcludeChunks(confg, htmlEntry) {
    const styleEntries = GradleBuild.config.html[htmlEntry].style
    
    const allEntries = GradleBuild.config.style

    const result = []
    Object.keys(allEntries).forEach(key => {
      if (styleEntries.indexOf(key) < 0) {
        result.push(key)
      }
    })
    return result
  }

  /**
   * create exclude chunks for jsEntry
   */
  createExcludeCunks(config, htmlEntry) {
    
    const result = []
    const jsEntries = GradleBuild.config.html[htmlEntry].application
    const entries = config.entry
    Object.keys(entries).forEach(key => {
      if (jsEntries.indexOf(key) < 0) {
          result.push(key)
      }
    })
    result.push(...this.createStyleExcludeChunks(config, htmlEntry)) 
    return result
  }
  /**
   * set html-webpack-plugin up
   */
  setupHtmlPlugin(config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin')
    const htmlConfig = GradleBuild.config.html
    const self = this
    for (const key in htmlConfig) {
      const config0 = {
        inject: false,
        cdnModule: key,
        minify: false,
        filename: htmlConfig[key].outputName,
        template: htmlConfig[key].source,
        excludeChunks: self.createExcludeCunks(config, key)
      }
      config.plugins.push(new HtmlWebpackPlugin(config0))
    }
  }
  
  /**
   * set webpack-cdn-plugin up.
   */
  setupCdnPlugin(config) {
    const WebpackCdnPlugin = require('webpack-cdn-plugin');
    const fontawesomeFree = {
      name: '@fortawesome/fontawesome-free',
      cdn: 'font-awesome',
      cssOnly: true,
      paths: [
      ],
      prodUrl: '//cdnjs.cloudflare.com/ajax/libs/:name/:version/:path',
      styles: [
        'css/fontawesome.css',
        'css/solid.css',
        'css/brands.css'
      ]
    }
    const basicModuleSetting = [
      {
        name: 'kotlin',
        path: 'kotlin.js'
      },
      {
        name: 'kotlinx-coroutines-core',
        path: 'kotlin-coroutines-core.js'
      },
      {
        name: 'jquery'
      },
      {
        name: '@popperjs/core',
        prodUrl: '//unpkg.com/:name@:version/dist/umd/popper.js'

      },
      {
        name: 'bootstrap',
        styles: [
          'dist/css/bootstrap.min.css'
        ]
      },
      fontawesomeFree
    ]

    const htmlConfig = GradleBuild.config.html
    const modules = {}
    for (const key in htmlConfig) {  
      modules[key] = basicModuleSetting
    }
    const cdnPluginConfig = { 
      modules
    }
    cdnPluginConfig.pathToNodeModules = GradleBuild.config.jsRootDir
    config.plugins = config.plugins || []
    config.plugins.push(new WebpackCdnPlugin(cdnPluginConfig))
  }

}

(function(config) {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const htmlWebpack = new HtmlWebpack()
    htmlWebpack.setupWebpack(config)
  }
})(config)



// vi: se ts=2 sw=2 et:
