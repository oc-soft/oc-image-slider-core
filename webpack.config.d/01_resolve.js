
class Resolve {


  /**
   * setup configuration
   */
  setup(config) {
    this.setupAlias(config)
  }
  /**
   * setup alias
   */
  setupAlias(config) {
    const resolve = config.resolve || {}
    const alias = resolve.alias || {}
 

    alias.kotlin = './kotlin.js'
    
    resolve.alias = alias
    config.resolve = resolve
  }


}

((config)=> {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const resolve = new Resolve()
    resolve.setup(config)
  }
})(config)
// vi: se ts=2 sw=2 et:
