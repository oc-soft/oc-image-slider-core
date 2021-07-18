

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
    this.setupHtmlPluginPostsMgr(config)
    this.setupHtmlPluginPost(config)
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
      /mainCss.*\.js/,
      /siteMgr.*\.js/,
      /siteMgrCss.*\.(js|css)/,
      /postsMgr.*\.js/,
      /postsMgrCss.*\.(js|css)/,
      /post.*\.js/,
      /postCss.*\.(js|css)/
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
      /mainCss.*\.(js|css)/,
      /main.*\.js/,
      /siteMgrCss.*\.js/,
      /postsMgr.*\.js/,
      /postsMgrCss.*\.(js|css)/,
      /post.*\.js/,
      /postCss.*\.(js|css)/
    ]

    config.plugins = config.plugins || []
    config.plugins.push(new HtmlWebpackPlugin(htmlPluginConfig))
    this.setupHtmlExcludeAssets(config)

  }

  /**
   * set html-webpack-plugin up
   */
  setupHtmlPluginPostsMgr(config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin');
   
    const htmlPluginConfig = {
      inject: false,
      cdnModule: 'postsMgr',
      minify: false
    }
    htmlPluginConfig.filename = GradleBuild.config.postsMgrHtmlOutput
    htmlPluginConfig.template = GradleBuild.config.postsMgrSrcTemplate

    htmlPluginConfig.excludeAssets = [
      /mainCss.*\.(js|css)/,
      /main.*\.js/,
      /siteMgr.*\.js/,
      /siteMgrCss.*\.(js|css)/,
      /postsMgrCss.*\.js/,
      /post-.*\.js/,
      /postCss.*\.(js|css)/
    ]

    config.plugins = config.plugins || []
    config.plugins.push(new HtmlWebpackPlugin(htmlPluginConfig))
    this.setupHtmlExcludeAssets(config)
  }

  /**
   * set html-webpack-plugin up
   */
  setupHtmlPluginPost(config) {
    const HtmlWebpackPlugin = require('html-webpack-plugin');
   
    const htmlPluginConfig = {
      inject: false,
      cdnModule: 'post',
      minify: false
    }
    htmlPluginConfig.filename = GradleBuild.config.postHtmlOutput
    htmlPluginConfig.template = GradleBuild.config.postSrcTemplate

    htmlPluginConfig.excludeAssets = [
      /mainCss.*\.(js|css)/,
      /main.*\.js/,
      /siteMgr.*\.js/,
      /siteMgrCss.*\.(js|css)/,
      /postsMgr.*\.js/,
      /postsMgrCss.*\.(js|css)/,
      /postCss.*\.js/
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
        siteMgr: basicModuleSetting,
        postsMgr: basicModuleSetting,
        post: basicModuleSetting
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
