

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
    this.setupHtmlPluginSiteMgr(config)
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
    htmlPluginConfig.excludeAssets = [
      /siteMgrCss.*\.css/,
      /mainCss.*\.js/,
      /siteMgr.*\.js/,
      /siteMgrCss.*\.js/
    ]
    config.plugins = config.plugins || []
    config.plugins.push(new HtmlWebpackPlugin(htmlPluginConfig))
    this.setupHtmlExcludeAssets(config)
  }
  
  /**
   * set html-webpack-plugin up
   */
  setupHtmlPluginSiteMgr(config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin');
   
    const htmlPluginConfig = {
      inject: false,
      cdnModule: 'siteMgr',
      minify: false
    }
    htmlPluginConfig.filename = GradleBuild.config.siteMgrHtmlOutput
    htmlPluginConfig.template = GradleBuild.config.siteMgrSrcTemplate

    htmlPluginConfig.excludeAssets = [
      /mainCss.*\.css/,
      /mainCss.*\.js/,
      /main.*\.js/,
      /siteMgrCss.*\.js/
    ]

    config.plugins = config.plugins || []
    config.plugins.push(new HtmlWebpackPlugin(htmlPluginConfig))
    this.setupHtmlExcludeAssets(config)

  }

  setupHtmlExcludeAssets(config) {
    const HtmlWebpackSkipAssetsPlugin = require(
      'html-webpack-skip-assets-plugin').HtmlWebpackSkipAssetsPlugin
    config.plugins = config.plugins || []
    config.plugins.push(new HtmlWebpackSkipAssetsPlugin())
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
    const basicModuleSetting = [
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
    const cdnPluginConfig = {
      modules: {
        main: basicModuleSetting,
        siteMgr: basicModuleSetting
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
