folder('build-status') {
    icon {
        buildStatusFolderIcon {
            jobs(['main', 'dev'] as Set)
        }
    }
}