/**
 * output 
 */
class Output{

  /**
   * setup config 
   */
  setupConfig(config) {
    config.output = config.output || { }

    config.output.filename = '[name].js'
 
  }
}

((config) => {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const output = new Output()
    output.setupConfig(config)
  }
})(config)

// vi: se ts=2 sw=2 et:
