
class Module {
  setupConfig(config) {
    this.setupExternals(config)
    this.setupRules(config)
  }

  setupExternals(config) {
    config.externals = config.externals || [] 
    
    config.externals.push({
      '@popperjs/core': {
          commonjs: '@popperjs/core',
          commonjs2: '@popperjs/core',
          root: 'Popper'
      }
    })
  }
 
  setupRules(config) {
    const MiniCssExtractPlugin = require('mini-css-extract-plugin')
    config.module = config.module || {}
    config.module.rules = config.module.rules || []

    config.module.rules.push({
      test: /\.scss$/i,
      
      use: [
        MiniCssExtractPlugin.loader, 
        {
          loader: 'css-loader', 
          options: {
            url: false
          }
        },
        'sass-loader'
      ]
    })
  }
}


((config) => {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const module = new Module()
    module.setupConfig(config)
  }

})(config)


// vi: se ts=2 sw=2 et:
