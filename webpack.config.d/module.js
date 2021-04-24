
class Module {


  setupConfig(config) {
    const MiniCssExtractPlugin = require('mini-css-extract-plugin')
    config.module = config.module || {}
    config.module.rules = config.module.rules || []

    config.module.rules.push({
      test: /\.scss$/i,
      
      use: [MiniCssExtractPlugin.loader, 'css-loader', 'sass-loader']
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
