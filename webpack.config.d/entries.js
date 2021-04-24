
/**
 *  entries 
 */
class Entries {

  /**
   * setup config 
   */
  setupConfig(config) {
    const path = require('path')
    config.entry = config.entry || { }
    config.entry.mainCss = GradleBuild.config.mainSrcCss
  }
}

((config) => {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const entries = new Entries()
    entries.setupConfig(config)
  }
})(config)

// vi: se ts=2 sw=2 et:
