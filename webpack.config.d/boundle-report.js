
/**
 * report bundle report
 */
class BundleReport {
  

  /**
   * output file
   */
  get outputFile() {
    return this.option.outputFile || 'bundle-report.json'
  }

  /**
   * constructor
   */
  constructor(option) {
    this.option = option
  }

  /**
   * apply
   */
  apply(compiler) {
    compiler.hooks.done.tap(
      'BundleReport',
      (stats) => {
        const bundles = {}

        bundles.externals = this.getExternalModules(
          stats.compilation).map(elem => elem.request)
        bundles.emittedAssets = []
        for (const elem of stats.compilation.emittedAssets) {
          bundles.emittedAssets.push(elem)
        }

        const path = require('path')
        const outputFile = path.join(compiler.outputPath, this.outputFile)

        compiler.outputFileSystem.writeFile(outputFile,
          JSON.stringify(bundles))


    })
  }


  /**
   * get external modules
   */
  getExternalModules(compilation) {
    const result = []
    compilation.modules.forEach(elem => {
      if (elem instanceof compilation.compiler.webpack.ExternalModule) {
          result.push(elem)
      }
    })
    return result
  }

  /**
   * setup
   */
  setup(config) {
    config.plugins = config.plugins || []
    config.plugins.push(this)
  }
}


((config) => {
  const bundleReport = new BundleReport({}) 
  bundleReport.setup(config)
})(config)

// vi: sw=2 ts=2 et:
