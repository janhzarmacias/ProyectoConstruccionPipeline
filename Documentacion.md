# Documentación Técnica - Práctica 2

Esta documentación técnica describe las funcionalidades implementadas en la Práctica 2 de la aplicación, centradas en la gestión de usuarios, control de accesos y privilegios administrativos. Está orientada al equipo de desarrollo, no al usuario final, con el objetivo de facilitar la comprensión, mantenimiento y evolución del proyecto.

---

## Funcionalidades implementadas

### 1. Barra de navegación (Obligatoria)
Se incorporó una barra de menú común a todas las vistas mediante un fragmento Thymeleaf reutilizable. Este componente proporciona enlaces de navegación clave como el acceso al listado de usuarios, la página principal y la opción de cerrar sesión.

**Archivo creado o modificado:**
- `templates/fragments.html`

**Código relevante:**
```html
<nav th:fragment="navbar" class="navbar navbar-expand-lg navbar-light bg-light">
    <a class="navbar-brand" th:href="@{/}">ToDoList</a>
    <div class="navbar-nav">
        <a class="nav-link" th:href="@{/usuarios}">Usuarios</a>
        <a class="nav-link" th:href="@{/logout}">Logout</a>
    </div>
</nav>
```
### 2. Listado de usuarios (Obligatoria)

Se implementó una vista accesible desde la ruta `/usuarios` que muestra todos los usuarios registrados en el sistema. Esta funcionalidad forma parte del núcleo de la aplicación y permite al usuario autenticado visualizar de forma tabular los datos básicos de todos los usuarios existentes. Está protegida mediante sesión para asegurar que sólo los usuarios autenticados puedan acceder.

---

**Archivo añadido:**
- `templates/usuarios/listaUsuarios.html`

---

**Método en el controlador:**
```java
@GetMapping("/usuarios")
public String listadoUsuarios(Model model) {
    if (!managerUserSession.usuarioLogeado()) {
        return "redirect:/login";
    }
    List<UsuarioData> usuarios = usuarioService.allUsuarios();
    model.addAttribute("usuarios", usuarios);
    return "usuarios/listaUsuarios";
}
```
**Vista:**
``` html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Listado de Usuarios</title>
</head>
<body>
    <div th:replace="fragments :: navbar"></div>
    <h1>Usuarios registrados</h1>
    <table>
        <thead>
            <tr>
                <th>ID</th>
                <th>Nombre</th>
                <th>Email</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="usuario : ${usuarios}">
                <td th:text="${usuario.id}">1</td>
                <td th:text="${usuario.nombre}">Nombre</td>
                <td th:text="${usuario.email}">email@example.com</td>
                <td>
                    <a th:href="@{'/usuarios/' + ${usuario.id}}">Ver detalle</a>
                </td>
            </tr>
        </tbody>
    </table>
</body>
</html>
```
### 3. Descripción de usuario (Obligatoria)

Se implementó una página de detalle accesible mediante la ruta `/usuarios/{id}`, donde `{id}` representa el identificador del usuario. Esta vista permite visualizar información detallada del usuario seleccionado, como nombre, email, rol, y estado (bloqueado o no). La página está protegida por sesión, garantizando que solo usuarios autenticados puedan acceder.

---

**Archivo añadido:**
- `templates/usuarios/detalleUsuario.html`

---

**Método en el controlador:**
```java
@GetMapping("/usuarios/{id}")
public String detalleUsuario(@PathVariable Long id, Model model) {
    if (!managerUserSession.usuarioLogeado()) {
        return "redirect:/login";
    }
    UsuarioData usuario = usuarioService.findById(id);
    model.addAttribute("usuario", usuario);
    return "usuarios/detalleUsuario";
}
```
**Vista:**
``` html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Detalle de Usuario</title>
</head>
<body>
    <div th:replace="fragments :: navbar"></div>
    <h1>Detalle de Usuario</h1>
    <p><strong>ID:</strong> <span th:text="${usuario.id}">1</span></p>
    <p><strong>Nombre:</strong> <span th:text="${usuario.nombre}">Nombre</span></p>
    <p><strong>Email:</strong> <span th:text="${usuario.email}">email@example.com</span></p>
    <p><strong>Administrador:</strong> <span th:text="${usuario.administrador ? 'Sí' : 'No'}"></span></p>
    <p><strong>Bloqueado:</strong> <span th:text="${usuario.bloqueado ? 'Sí' : 'No'}"></span></p>
</body>
</html>
```
### 4. Usuario administrador (Opcional)

Se implementó el concepto de usuario administrador para controlar privilegios especiales dentro de la aplicación. Esto se realizó añadiendo un campo booleano `administrador` en la entidad `Usuario`. La aplicación garantiza que solo puede existir un único administrador en el sistema.

---

**Fragmento relevante en el servicio de usuario:**

```java
if (usuario.isAdministrador() && usuarioRepository.existsByAdministradorTrue()) {
    throw new RuntimeException("Ya existe un administrador.");
}
```
### Uso en el controlador o vistas: ###

``` html
<div th:if="${usuario.administrador}">
    <button>Bloquear usuario</button>
</div>
````
``` java
if (!usuarioLogueado.isAdministrador()) {
    return "redirect:/acceso-denegado";
}
```
### 5. Protección de vistas (Opcional)

Se implementó un mecanismo de protección para las vistas que muestran el listado y la descripción de usuarios, asegurando que sólo los usuarios autenticados puedan acceder a estas páginas.

---

**Mecanismo implementado:**

- Se utiliza un servicio llamado `managerUserSession` que gestiona la sesión del usuario.
- Antes de procesar cualquier solicitud para las vistas protegidas, se verifica si el usuario está autenticado.
- Si no hay un usuario logueado, se redirige automáticamente a la página de login.

---

**Fragmento común utilizado en los controladores:**

```java
if (!managerUserSession.usuarioLogeado()) {
    return "redirect:/login";
}
```
### 6. Bloqueo de usuarios por administrador (Opcional)

Se implementó una funcionalidad que permite a los administradores bloquear o desbloquear el acceso de otros usuarios al sistema. Los usuarios bloqueados no pueden iniciar sesión ni acceder a las funcionalidades protegidas.

---

**Detalles de la implementación:**

- Se añadió un campo booleano `bloqueado` en la entidad `Usuario` para representar el estado de bloqueo.
- Desde la vista de detalle de cada usuario, el administrador puede activar o desactivar este estado.
- Durante el proceso de autenticación, se verifica si el usuario está bloqueado, y en ese caso se muestra un mensaje de error y se evita el acceso.

---

**Verificación en el controlador de login:**

```java
if (usuario.isBloqueado()) {
    model.addAttribute("error", "Usuario bloqueado. Contacta con el administrador.");
    return "formLogin";
}
```
**Servicio para bloquear usuarios:**
``` java
@Transactional
public void toggleBloqueoUsuario(Long usuarioId) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    usuario.setBloqueado(!usuario.isBloqueado());
    usuarioRepository.save(usuario);
}
```


