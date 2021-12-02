
/**
 *  entries 
 */
class Entries {

  /**
   * setup config 
   */
  setupConfig(config) {
    config.entry = config.entry || { }



    Object.assign(config.entry, GradleBuild.config.js[config.mode])
    Object.assign(config.entry, GradleBuild.config.style)
 
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
