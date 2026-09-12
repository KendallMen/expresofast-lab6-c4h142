USE [ExpresoFast_C4H142_II2026];
GO

-- The BCrypt hash below is for the password: Password123!
DECLARE @passwordHash VARCHAR(255) = '$2a$10$e0MYzXyjpJS7Pd0RVvHwHe1Wn5cGBwA/7XgOymx1i86Kx5w5zK7y6';

IF NOT EXISTS (SELECT 1 FROM dbo.Rol WHERE nombre_rol = 'ROLE_ADMIN')
    INSERT INTO dbo.Rol (nombre_rol) VALUES ('ROLE_ADMIN');
IF NOT EXISTS (SELECT 1 FROM dbo.Rol WHERE nombre_rol = 'ROLE_OPERADOR')
    INSERT INTO dbo.Rol (nombre_rol) VALUES ('ROLE_OPERADOR');
IF NOT EXISTS (SELECT 1 FROM dbo.Rol WHERE nombre_rol = 'ROLE_CONDUCTOR')
    INSERT INTO dbo.Rol (nombre_rol) VALUES ('ROLE_CONDUCTOR');

IF NOT EXISTS (SELECT 1 FROM dbo.Usuario WHERE username = 'admin')
    INSERT INTO dbo.Usuario (username, password_hash, nombre_completo, email, activo)
    VALUES ('admin', @passwordHash, 'Administrador ExpresoFast', 'admin@expresofast.cr', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.Usuario WHERE username = 'operador1')
    INSERT INTO dbo.Usuario (username, password_hash, nombre_completo, email, activo)
    VALUES ('operador1', @passwordHash, 'Operador ExpresoFast', 'operador1@expresofast.cr', 1);
IF NOT EXISTS (SELECT 1 FROM dbo.Usuario WHERE username = 'conductor1')
    INSERT INTO dbo.Usuario (username, password_hash, nombre_completo, email, activo)
    VALUES ('conductor1', @passwordHash, 'Conductor ExpresoFast', 'conductor1@expresofast.cr', 1);

INSERT INTO dbo.UsuarioRol (usuario_id, rol_id)
SELECT u.usuario_id, r.rol_id
FROM dbo.Usuario u
CROSS JOIN dbo.Rol r
WHERE (
             (u.username = 'admin' AND r.nombre_rol = 'ROLE_ADMIN')
        OR (u.username = 'operador1' AND r.nombre_rol = 'ROLE_OPERADOR')
        OR (u.username = 'conductor1' AND r.nombre_rol = 'ROLE_CONDUCTOR')
)
    AND NOT EXISTS (
      SELECT 1 FROM dbo.UsuarioRol ur
      WHERE ur.usuario_id = u.usuario_id AND ur.rol_id = r.rol_id
  );
GO

IF NOT EXISTS (SELECT 1 FROM dbo.EmpresaLogistica WHERE cedula_juridica = '310112345678')
    INSERT INTO dbo.EmpresaLogistica (nombre, cedula_juridica, telefono)
    VALUES ('Transportes Rapidos UCR', '310112345678', '88881111');
IF NOT EXISTS (SELECT 1 FROM dbo.EmpresaLogistica WHERE cedula_juridica = '310198765432')
    INSERT INTO dbo.EmpresaLogistica (nombre, cedula_juridica, telefono)
    VALUES ('Logistica Central CR', '310198765432', '87772222');

IF NOT EXISTS (SELECT 1 FROM dbo.Conductor WHERE licencia = 'LIC-1001')
    INSERT INTO dbo.Conductor (nombre, apellidos, licencia, telefono)
    VALUES ('Carlos', 'Rodriguez Vargas', 'LIC-1001', '88883333');
IF NOT EXISTS (SELECT 1 FROM dbo.Conductor WHERE licencia = 'LIC-1002')
    INSERT INTO dbo.Conductor (nombre, apellidos, licencia, telefono)
    VALUES ('Maria', 'Solano Perez', 'LIC-1002', '87774444');

INSERT INTO dbo.Vehiculo (placa, capacidad_kg, estado, empresa_id)
SELECT 'ABC123', 1500.00, 'Disponible', e.empresa_id
FROM dbo.EmpresaLogistica e
WHERE e.cedula_juridica = '310112345678'
  AND NOT EXISTS (SELECT 1 FROM dbo.Vehiculo v WHERE v.placa = 'ABC123');

INSERT INTO dbo.Vehiculo (placa, capacidad_kg, estado, empresa_id)
SELECT 'XYZ789', 3000.00, 'Disponible', e.empresa_id
FROM dbo.EmpresaLogistica e
WHERE e.cedula_juridica = '310198765432'
  AND NOT EXISTS (SELECT 1 FROM dbo.Vehiculo v WHERE v.placa = 'XYZ789');
GO

IF NOT EXISTS (SELECT 1 FROM dbo.Envio WHERE codigo_rastreo = 'EXP-0001')
BEGIN
    INSERT INTO dbo.Envio
        (codigo_rastreo, direccion_destino, peso_kg, costo, estado_envio,
         vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
    SELECT 'EXP-0001', 'Cartago, Paraiso, UCR', 8.50, 4500.00, 'PENDIENTE',
           v.vehiculo_id, c.conductor_id, SYSDATETIME(), SYSDATETIME()
    FROM dbo.Vehiculo v
    CROSS JOIN dbo.Conductor c
    WHERE v.placa = 'ABC123' AND c.licencia = 'LIC-1001';
END;

IF NOT EXISTS (SELECT 1 FROM dbo.Envio WHERE codigo_rastreo = 'EXP-0002')
BEGIN
    INSERT INTO dbo.Envio
        (codigo_rastreo, direccion_destino, peso_kg, costo, estado_envio,
         vehiculo_id, conductor_id, fecha_creacion, fecha_modificacion)
    SELECT 'EXP-0002', 'San Jose, Curridabat', 15.00, 7500.00, 'PENDIENTE',
           v.vehiculo_id, c.conductor_id, SYSDATETIME(), SYSDATETIME()
    FROM dbo.Vehiculo v
    CROSS JOIN dbo.Conductor c
    WHERE v.placa = 'XYZ789' AND c.licencia = 'LIC-1002';
END;
GO
