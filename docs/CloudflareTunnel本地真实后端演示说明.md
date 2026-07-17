# Cloudflare Tunnel 本地真实后端演示说明

## 目标结构

- 前端生产包：`pm-frontend/dist`
- Spring Boot 后端：`127.0.0.1:8081`
- Nginx 统一入口：`127.0.0.1:8080`
- Cloudflare Tunnel：公开 `http://localhost:8080`

这样浏览器访问同一个公网地址即可同时使用前端页面和后端接口：

- 页面：`/`
- 接口：`/api/...`

## 1. 构建真实后端版本前端

```powershell
cd I:\规划院\项目管理系统\pm-frontend
npm run build:real
```

当前生产环境配置为：

```env
VITE_API_BASE_URL=/api
VITE_MOCK=false
```

如果还需要保留原来的 Netlify 演示假数据版本，可以使用：

```powershell
npm run build:demo
```

## 2. 启动后端到 8081

避免和 Nginx 的 8080 冲突，Spring Boot 建议使用 8081：

```powershell
$env:SERVER_PORT="8081"
```

然后按你平时的方式启动 Java 后端。

## 3. 启动 Nginx

Nginx 配置参考：

```text
deploy/nginx/pm-system-cloudflared.conf
```

确认 Nginx 监听：

```text
http://127.0.0.1:8080
```

本地浏览器先访问这个地址，能登录并正常请求真实接口后，再启动 Cloudflare Tunnel。

## 4. 启动 Cloudflare Tunnel

```powershell
cloudflared tunnel --url http://localhost:8080
```

终端会输出一个公网地址。把这个地址发给领导或测试人员即可访问本机真实后端版本。

## 注意事项

1. Quick Tunnel 地址通常是随机的，重启后可能变化。
2. 电脑关机、后端停止、Nginx 停止、cloudflared 停止，公网地址都会不可用。
3. 小范围演示可以用 Quick Tunnel；长期试运行建议改成 Cloudflare Named Tunnel 和固定域名。
4. 真实使用前不要公开默认测试账号密码。
