USE [ExpresoFast_C4H142_II2026];
GO

IF OBJECT_ID(N'dbo.Usuario', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Usuario (
        usuario_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Usuario PRIMARY KEY,
        username VARCHAR(50) NOT NULL CONSTRAINT UQ_Usuario_username UNIQUE,
        password_hash VARCHAR(255) NOT NULL,
        nombre_completo VARCHAR(100) NOT NULL,
        email VARCHAR(100) NOT NULL CONSTRAINT UQ_Usuario_email UNIQUE,
        activo BIT NOT NULL CONSTRAINT DF_Usuario_activo DEFAULT 1
    );
END;
GO

IF OBJECT_ID(N'dbo.Rol', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.Rol (
        rol_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Rol PRIMARY KEY,
        nombre_rol VARCHAR(30) NOT NULL CONSTRAINT UQ_Rol_nombre UNIQUE,
        CONSTRAINT CK_Rol_nombre CHECK (nombre_rol IN ('ROLE_ADMIN', 'ROLE_OPERADOR', 'ROLE_CONDUCTOR'))
    );
END;
GO

IF OBJECT_ID(N'dbo.UsuarioRol', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.UsuarioRol (
        usuario_id INT NOT NULL,
        rol_id INT NOT NULL,
        CONSTRAINT PK_UsuarioRol PRIMARY KEY (usuario_id, rol_id),
        CONSTRAINT FK_UsuarioRol_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.Usuario(usuario_id),
        CONSTRAINT FK_UsuarioRol_rol FOREIGN KEY (rol_id) REFERENCES dbo.Rol(rol_id)
    );
END;
GO

IF OBJECT_ID(N'dbo.BitacoraEnvio', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.BitacoraEnvio (
        bitacora_id INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_BitacoraEnvio PRIMARY KEY,
        envio_id INT NOT NULL,
        estado_anterior VARCHAR(20) NOT NULL,
        estado_nuevo VARCHAR(20) NOT NULL,
        fecha_cambio DATETIME2 NOT NULL,
        usuario_id INT NOT NULL,
        observaciones VARCHAR(250) NULL,
        CONSTRAINT FK_BitacoraEnvio_envio FOREIGN KEY (envio_id) REFERENCES dbo.Envio(envio_id),
        CONSTRAINT FK_BitacoraEnvio_usuario FOREIGN KEY (usuario_id) REFERENCES dbo.Usuario(usuario_id)
    );
END;
GO
