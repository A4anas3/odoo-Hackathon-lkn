import React from 'react';
import { useAuth } from '../../context/AuthContext';

/**
 * PermissionGuard
 * Conditionally renders children if the authenticated user has any of the required role(s).
 *
 * Usage:
 * <PermissionGuard roles={['ADMIN', 'STAFF']} fallback={<p>Access Denied</p>}>
 *   <SensitiveFeatureComponent />
 * </PermissionGuard>
 */
export const PermissionGuard = ({ roles, permissions, children, fallback = null }) => {
  const { hasRole, hasPermission } = useAuth();

  if (roles) {
    if (!hasRole(roles)) {
      return fallback;
    }
  }

  if (permissions) {
    if (!hasPermission(permissions)) {
      return fallback;
    }
  }

  return <>{children}</>;
};

export default PermissionGuard;

