package com.ponyvillesquare.speed;

import com.mumfrey.liteloader.Permissible;
import com.mumfrey.liteloader.permissions.Permissions;
import com.mumfrey.liteloader.permissions.PermissionsManagerClient;

public class PermissionManager implements IPermissions {

    private final Permissions permissions;
    private boolean rights;

    public PermissionManager(Permissions permissions) {
        this.permissions = permissions;
    }

    @Override
    public boolean can(Perms s) {
        return permissions.getHasPermission(s.toString().toLowerCase(), true);
    }

    @Override
    public boolean hasRights() {
        return this.rights;
    }

    private boolean testForRights() {
        boolean rights = false;
        for (Perms perm : Perms.values()) {
            rights |= can(perm);
        }
        return rights;
    }

    static void registerPermissions(PermissionsManagerClient permManager, Permissible mod) {
        for (Perms perm : Perms.values()) {
            permManager.registerModPermission(mod, perm.toString().toLowerCase());
        }
    }

    void onChanged() {
        this.rights = testForRights();
    }

    public static IPermissions offline() {
        return new IPermissions() {
            @Override
            public boolean can(Perms s) {
                return true;
            }

            @Override
            public boolean hasRights() {
                return true;
            }
        };
    }

}
