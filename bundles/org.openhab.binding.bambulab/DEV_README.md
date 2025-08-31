# Bambu API

## Log in

```http request
POST https://api.bambulab.com/v1/user-service/user/login
Content-Type: application/json

{
  "account": "bambus@grzeslowski.pl",
  "password": "xyz"
}
```

### Good password

```json
{
  "accessToken": "",
  "refreshToken": "",
  "expiresIn": 0,
  "refreshExpiresIn": 0,
  "tfaKey": "",
  "accessMethod": "",
  "loginType": "verifyCode"
}
```

### Wrong password

```json
{
  "code": 2,
  "error": "Incorrect password"
}
```

## Access Code

```http request
POST https://api.bambulab.com/v1/user-service/user/login
Content-Type: application/json

{
  "account": "bambus@grzeslowski.pl",
  "code": "123456"
}
```

### Good code

```json
{
  "accessToken": "access-token",
  "refreshToken": "refresh-token",
  "expiresIn": 7776000,
  "refreshExpiresIn": 7776000,
  "tfaKey": "",
  "accessMethod": "",
  "loginType": ""
}
```

### Wrong code

```json
{
  "code": 2,
  "error": "Incorrect code"
}
```
