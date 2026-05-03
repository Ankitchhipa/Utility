package com.itl.commonres.permissions

class PermissionResult {
    var permissionStatus: HashMap<String, PermissionStatus> = hashMapOf()
    var finalStatus: PermissionStatus = PermissionStatus.NOT_GIVEN
}