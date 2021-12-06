
/**
 * setup externals 
 */
class Externals {

  /**
   * setup webpack configuration
   */
  setupConfig(config) {
    config.externals = config.externals || []

    let externalObj = undefined
    if (Array.isArray(config.externals)) {
      externalObj = config.externals.find(elem => typeof elem === 'object')
      if (typeof externalObj === 'undefined') {
        externalObj = { }
        config.externals.push(externalObj)
      }
    } else if (typeof config.externals === 'object') {
      externalObj = config.externals
    } else if (typeof config.externals === 'string') {
      externalObj = {}
      externalObj[config.externals] = config.externals
      config.externals = externalObj
    } else if (typeof config.externals === 'function') {
      externalObj = { }
      config.externals = [ config.externals, externalObj ]
    }

    Reflect.ownKeys(GradleBuild.config.htmlCdn).forEach(key => {
      const entry = GradleBuild.config.htmlCdn[key]
      let extEntry = undefined
      if ('external' in entry) {
        externalObj[key] = entry.external
      } else {
        externalObj[key] = key
      }
    })
  }
}

((config) => {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const externals = new Externals()
    externals.setupConfig(config)
  }

})(config)


// vi: se ts=2 sw=2 et:
