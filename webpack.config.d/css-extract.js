

class CssExtract {

  setupConfig(config) {
    const MiniCssExtractPlugin = require('mini-css-extract-plugin')

    config.plugins = config.plugins || []
    
    const cssDir = GradleBuild.config.cssDir
    config.plugins.push(new MiniCssExtractPlugin({
      filename: `${cssDir}/[name].css`
    }))


  }
}

((config) => {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const cssExtract = new CssExtract()
    cssExtract.setupConfig(config)
  }
})(config)


// vi: se ts=2 sw=2 et:
