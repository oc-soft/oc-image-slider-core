

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
   * set html-webpack-plugin up
   */
  setupHtmlPlugin(config) {
    this.setupHtmlPluginMain(config)
  }

  /**
   * set html-webpack-plugin up
   */
  setupHtmlPluginMain(config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin');
    const htmlPluginConfig = {
      inject: false,
      cdnModule: 'main',
      minify: false
    }
    htmlPluginConfig.filename = GradleBuild.config.mainHtmlOutput
    htmlPluginConfig.template = GradleBuild.config.mainSrcTemplate

    config.plugins = config.plugins || []
    config.plugins.push(new HtmlWebpackPlugin(htmlPluginConfig))
  }

  /**
   * set webpack-cdn-plugin up.
   */
  setupCdnPlugin(config) {
    const WebpackCdnPlugin = require('webpack-cdn-plugin');
    const fontawesomeFree = {
      name: '@fortawesome/fontawesome-free',
      cdn: 'font-awesome',
      paths: [
        'js/fontawesome.min.js',
        'js/solid.js',
        'js/brands.js'
      ],
      prodUrl: '//cdnjs.cloudflare.com/ajax/libs/:name/:version/:path',
      styles: [
        'css/fontawesome.css',
        'css/solid.css',
        'css/brands.css'
      ]
    }
    const cdnPluginConfig = {
      modules: {
        main: [
          {
            name: 'kotlin'
          },
          {
            name: 'kotlinx-coroutines-core'
          },
          {
            name: 'jquery'
          },
          {
            name: 'bootstrap',
            styles: [
              'dist/css/bootstrap.min.css'
            ]
          },
          fontawesomeFree
        ]
      }
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
