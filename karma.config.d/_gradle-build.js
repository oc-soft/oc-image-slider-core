
class GradleBuild {
  static get config() {
    const result = {
    "js": {
        "development": {
            "post-mgr": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-postMgr/kotlin-dce-dev/wesgee-postMgr.js",
            "post": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-post/kotlin-dce-dev/wesgee-post.js",
            "message-mgr": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-messageMgr/kotlin-dce-dev/wesgee-messageMgr.js",
            "work-us-mgr": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-workUsMgr/kotlin-dce-dev/wesgee-workUsMgr.js"
        },
        "production": {
            "post-mgr": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-postMgr/kotlin-dce/wesgee-postMgr.js",
            "post": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-post/kotlin-dce/wesgee-post.js",
            "message-mgr": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-messageMgr/kotlin-dce/wesgee-messageMgr.js",
            "work-us-mgr": "/Users/toshiyuki/Projects/nbs/build/js/packages/wesgee-workUsMgr/kotlin-dce/wesgee-workUsMgr.js"
        }
    },
    "style": {
        "nbs-scss": "/Users/toshiyuki/Projects/nbs/src/site/style/main.scss",
        "post-mgr-scss": "/Users/toshiyuki/Projects/nbs/src/site/style/post-mgr.scss",
        "post-scss": "/Users/toshiyuki/Projects/nbs/src/site/style/post.scss",
        "message-mgr-scss": "/Users/toshiyuki/Projects/nbs/src/site/style/message-mgr.scss",
        "work-us-mgr": "/Users/toshiyuki/Projects/nbs/src/site/style/work-us-mgr.scss"
    },
    "cssOutput": {
        "nbs-scss": "nbs.css",
        "post-mgr-scss": "post-mgr.css",
        "post-scss": "post.css",
        "message-mgr-scss": "message-mgr.css",
        "work-us-mgr": "work-us-mgr.css"
    },
    "jsDir": "js",
    "cssDir": "css",
    "mainProgramName": "nbs",
    "jsRootDir": "/Users/toshiyuki/Projects/nbs/build/js",
    "html": {
        "index": {
            "application": [
                "main"
            ],
            "style": [
                "main-scss"
            ],
            "source": "/Users/toshiyuki/Projects/nbs/src/site/template/asset-tags-0.php",
            "outputName": "index-tags-0.php"
        },
        "post": {
            "application": [
                "post"
            ],
            "style": [
                "post-scss"
            ],
            "source": "/Users/toshiyuki/Projects/nbs/src/site/template/asset-tags-0.php",
            "outputName": "post-tags-0.php"
        },
        "message": {
            "application": [
                "message-mgr"
            ],
            "style": [
                "message-mgr-scss"
            ],
            "source": "/Users/toshiyuki/Projects/nbs/src/site/template/asset-tags-0.php",
            "outputName": "message-tags-0.php"
        }
    }
};
    return result;
  }
}



// vi: se ts=2 sw=2 et
