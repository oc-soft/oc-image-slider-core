
/**
 * keep to clean output directector 
 */
class Clean {

  setupConfig(config) {
    const { CleanWebpackPlugin } = require('clean-webpack-plugin')


    config.plugins = config.plugins || []
    config.plugins.push(
      new CleanWebpackPlugin())
  }
}

((config) => {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const clean = new Clean()
    clean.setupConfig(config)
  }


})(config)

// vi: se ts=2 sw=2 et:
