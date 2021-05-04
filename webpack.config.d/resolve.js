
class Resolve {



  setupConfig(config) {

    const path = require('path')
    config.resolve = config.resolve || {}
    config.resolve.modules = config.resolve.modules || []

    config.resolve.modules.push(
      path.join(GradleBuild.config.jsRootDir, 'node_modules'))
  }

}

((config)=> {
  const path = require('path')
  const pathInfo = path.parse(__filename)
  if (pathInfo.name != 'karma.conf') {
    const resolve = new Resolve()
    resolve.setupConfig(config)
  }
})(config)
// vi: se ts=2 sw=2 et:
