

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
   * create exclude chunks for jsEntry
   */
  createExcludeCunks(config, htmlEntry) {
    
    const result = []
    const htmlEntries = GradleBuild.config.html[htmlEntry].application
    htmlEntries.push(...GradleBuild.config.html[htmlEntry].style)

    const entries = config.entry
    Object.keys(entries).forEach(key => {
      if (htmlEntries.indexOf(key) < 0) {
          result.push(key)
      }
    })
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
    const htmlConfig = GradleBuild.config.html

    const cdnSetting = []
    Reflect.ownKeys(GradleBuild.config.htmlCdn).forEach(key => {
      const entry = GradleBuild.config.htmlCdn[key]
      const cdnEntry = { name: key }
      if ('cdn' in entry) {
        Object.assign(cdnEntry, entry.cdn)  
      }
      cdnSetting.push(cdnEntry)
    })
    const modules = {}
    for (const key in htmlConfig) {  
      modules[key] = cdnSetting
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
