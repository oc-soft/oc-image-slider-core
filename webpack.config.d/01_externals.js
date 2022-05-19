
/**
 * control externals 
 */
class Externals {

  /**
   * setup configuration
   */
  setup(config) {

    const externals = config.externals || { }

    externals['@wordpress/block-library'] = {
      root: ['wp', 'blockLibrary'],
      commonjs: '@wordpress/block-library',
      commonjs2: '@wordpress/block-library',
      amd: '@wordpress/block-library',
    }
    externals['@wordpress/blocks'] = {
      root: ['wp', 'blocks'],
      commonjs: '@wordpress/blocks',
      commonjs2: '@wordpress/blocks',
      amd: '@wordpress/blocks'
    }
    externals['@wordpress/block-editor'] = {
      root: ['wp', 'blockEditor'],
      commonjs: '@wordpress/block-editor',
      commonjs2: '@wordpress/block-editor',
      amd: '@wordpress/block-editor'
    }
    externals['@wordpress/compose'] = {
      root: ['wp', 'compose'],
      commonjs: '@wordpress/compose',
      commonjs2: '@wordpress/compose',
      amd: '@wordpress/compose'
    }
    externals['@wordpress/primitives'] = {
      root: ['wp', 'primitives'],
      commonjs: '@wordpress/primitives',
      commonjs2: '@wordpress/primitives',
      amd: '@wordpress/primitives'
    }
    externals['@wordpress/element'] = {
      root: ['wp', 'element'],
      commonjs: '@wordpress/element',
      commonjs2: '@wordpress/element',
      amd: '@wordpress/element'
    }
    externals['@wordpress/i18n'] = {
      root: ['wp', 'i18n'],
      commonjs: '@wordpress/i18n',
      commonjs2: '@wordpress/i18n',
      amd: '@wordpress/i18n'
    }
    externals['@wordpress/components'] = {
      root: ['wp', 'components'],
      commonjs: '@wordpress/components',
      commonjs2: '@wordpress/components',
      amd: '@wordpress/components'
    }
    externals['react'] = {
      root: 'React',
      commonjs: 'react',
      commonjs2: 'react',
      amd: 'react'
    }
    externals['react-dom'] = {
      root: 'ReactDom',
      commonjs: 'react-dom',
      commonjs2: 'react-dom',
      amd: 'react-dom'
    }



    config.externals = externals

  }
}


((config)=> {
  const externals = new Externals()
  externals.setup(config)
})(config)

// vi: se ts=2 sw=2 et:
