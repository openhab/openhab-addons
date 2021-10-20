const { AbstractProvider } = require('../provider');

const METADATA_PROVIDER_CLASS = "org.eclipse.smarthome.core.items.MetadataProvider";

class StaticCallbackMetadataProvider extends AbstractProvider {
    constructor(){
        super(METADATA_PROVIDER_CLASS);
        this.metadataCallbacks = [];
    }

    addProviderChangeListener(listener) {
    }

    removeProviderChangeListener(listener) {
    }

    addMetadataCallback(callback) {
        this.metadataCallbacks.push(callback);
    }

    getAll(){
        require('../log')('metadata-provider').debug("///"+this.metadataCallbacks.length);
        require('../log')('metadata-provider').debug("///"+this.metadataCallbacks.flatMap(c => c()).length);

        for(let x of this.metadataCallbacks.flatMap(c => c())) {
            require('../log')('metadata-provider').debug(x);
        }

        return utils.jsArrayToJavaList(this.metadataCallbacks.flatMap(c => c()));
    }
}

module.exports = {
    staticCallbackMetadataProvider: () => new StaticCallbackMetadataProvider()
    
}