
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
      config.entry.postsMgr = GradleBuild.config.postsMgrDevEntry
      config.entry.post = GradleBuild.config.postDevEntry,
      config.entry.messageMgr = GradleBuild.config.messageMgrDevEntry
      config.entry.workUsMgr = GradleBuild.config.workUsMgrDevEntry
    } else {
      config.entry.siteMgr = GradleBuild.config.siteMgrEntry
      config.entry.postsMgr = GradleBuild.config.postsMgrEntry
      config.entry.post = GradleBuild.config.postEntry
      config.entry.messageMgr = GradleBuild.config.messageMgrEntry
      config.entry.workUsMgr = GradleBuild.config.workUsMgrEntry
    }
 
    config.entry.mainCss = GradleBuild.config.mainSrcCss
    config.entry.siteMgrCss = GradleBuild.config.siteMgrSrcCss
    config.entry.postsMgrCss = GradleBuild.config.postsMgrSrcCss
    config.entry.postCss = GradleBuild.config.postSrcCss
    config.entry.messageMgrCss = GradleBuild.config.messageMgrSrcCss
    config.entry.workUsMgrCss = GradleBuild.config.workUsMgrSrcCss
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
