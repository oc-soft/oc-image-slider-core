
/**
 *  entries 
 */
class Entries {

  /**
   * setup config 
   */
  setupConfig(config) {
    config.entry = config.entry || { }

    if (config.mode == 'development') {
      config.entry.siteMgr = GradleBuild.config.siteMgrDevEntry
    } else {
      config.entry.siteMgr = GradleBuild.config.siteMgrEntry
    }
 
    config.entry.mainCss = GradleBuild.config.mainSrcCss
    config.entry.siteMgrCss = GradleBuild.config.siteMgrSrcCss
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
