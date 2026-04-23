import { useEffect, useMemo, useState } from 'react'
import ReactMarkdown from 'react-markdown'

const initialRecordForm = {
  content: '',
  date: new Date().toISOString().slice(0, 10),
  tags: '',
}

async function apiFetch(url, options = {}) {
  const response = await fetch(url, {
    credentials: 'include',
    ...options,
    headers: {
      ...(options.body instanceof FormData ? {} : { 'Content-Type': 'application/json' }),
      ...(options.headers || {}),
    },
  })

  const isJson = response.headers.get('content-type')?.includes('application/json')
  const payload = isJson ? await response.json() : null

  if (!response.ok) {
    throw new Error(payload?.message || '请求失败，请稍后重试。')
  }

  return payload
}

function Header({ home, auth, onRefresh, onLogout }) {
  return (
    <header className="surface overflow-hidden">
      <div className="flex flex-col gap-6 p-6 sm:p-8">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="space-y-3">
            <span className="pill">Baby Steps</span>
            <div>
              <h1 className="text-3xl font-medium tracking-tight text-slate-900 sm:text-5xl">
                {home?.babyName || 'Little Baby'} 的成长相册
              </h1>
              <p className="mt-3 max-w-2xl text-sm leading-7 text-slate-500 sm:text-base">
                去社交化、专注记录、数据自主。用一条安静的时间轴，留住每一次第一次、每一段月龄变化。
              </p>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              onClick={onRefresh}
              className="rounded-full border border-slate-200 px-4 py-2 text-sm text-slate-600 transition hover:border-slate-300 hover:text-slate-900"
            >
              刷新
            </button>
            {auth?.authenticated ? (
              <button
                type="button"
                onClick={onLogout}
                className="rounded-full bg-slate-900 px-4 py-2 text-sm text-white transition hover:bg-slate-700"
              >
                退出后台
              </button>
            ) : (
              <span className="pill">访客模式</span>
            )}
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-3">
          <StatCard label="出生日期" value={home?.birthDate || '--'} />
          <StatCard label="出生天数" value={home ? `${home.daysSinceBirth} 天` : '--'} />
          <StatCard label="当前月龄" value={home?.currentAgeLabel || '--'} />
        </div>
      </div>
    </header>
  )
}

function StatCard({ label, value }) {
  return (
    <div className="rounded-3xl border border-slate-200 bg-slate-50/80 p-5">
      <div className="text-sm text-slate-500">{label}</div>
      <div className="mt-3 text-2xl font-medium tracking-tight text-slate-900">{value}</div>
    </div>
  )
}

function LoginCard({ auth, loginForm, setLoginForm, onLogin, loginBusy }) {
  if (auth?.authenticated) {
    return (
      <section className="surface p-6 sm:p-7">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div>
            <h2 className="text-xl font-medium text-slate-900">后台已登录</h2>
            <p className="mt-2 text-sm text-slate-500">
              当前账号：<span className="font-medium text-slate-700">{auth.username}</span>
            </p>
          </div>
          <span className="pill">仅管理员可发布内容</span>
        </div>
      </section>
    )
  }

  return (
    <section className="surface p-6 sm:p-7">
      <div className="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
        <div className="max-w-lg space-y-2">
          <h2 className="text-xl font-medium text-slate-900">管理员登录</h2>
          <p className="text-sm leading-7 text-slate-500">
            使用 data/config/app-config.properties 中配置的账号密码登录，即可发布成长记录与上传照片。
          </p>
        </div>
        <form className="grid w-full gap-3 sm:grid-cols-[1fr_1fr_auto] lg:max-w-2xl" onSubmit={onLogin}>
          <input
            className="field"
            placeholder="用户名"
            value={loginForm.username}
            onChange={(event) => setLoginForm((current) => ({ ...current, username: event.target.value }))}
          />
          <input
            type="password"
            className="field"
            placeholder="密码"
            value={loginForm.password}
            onChange={(event) => setLoginForm((current) => ({ ...current, password: event.target.value }))}
          />
          <button
            type="submit"
            disabled={loginBusy}
            className="rounded-2xl bg-slate-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-slate-700 disabled:cursor-not-allowed disabled:bg-slate-300"
          >
            {loginBusy ? '登录中...' : '进入后台'}
          </button>
        </form>
      </div>
    </section>
  )
}

function AdminPanel({ settings, form, setForm, files, setFiles, submitting, onSubmit }) {
  if (!settings) {
    return null
  }

  return (
    <section className="surface p-6 sm:p-8">
      <div className="grid gap-6 xl:grid-cols-[0.92fr_1.08fr]">
        <div className="space-y-4">
          <div>
            <h2 className="text-xl font-medium text-slate-900">后台发布</h2>
            <p className="mt-2 text-sm leading-7 text-slate-500">
              支持 Markdown 文本、日期选择、多图上传，以及逗号分隔的标签分类。
            </p>
          </div>
          <div className="rounded-3xl border border-slate-200 bg-slate-50/80 p-5 text-sm text-slate-600">
            <div className="flex items-center justify-between gap-3">
              <span>宝宝姓名</span>
              <span className="font-medium text-slate-900">{settings.babyName}</span>
            </div>
            <div className="mt-3 flex items-center justify-between gap-3">
              <span>生日</span>
              <span className="font-medium text-slate-900">{settings.birthDate}</span>
            </div>
            <div className="mt-3 flex items-center justify-between gap-3">
              <span>管理员账号</span>
              <span className="font-medium text-slate-900">{settings.adminUsername}</span>
            </div>
          </div>
        </div>

        <form className="space-y-4" onSubmit={onSubmit}>
          <textarea
            className="field min-h-44 resize-y"
            placeholder="今天发生了什么？支持 Markdown。"
            value={form.content}
            onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))}
          />
          <div className="grid gap-4 sm:grid-cols-2">
            <label className="space-y-2 text-sm text-slate-500">
              <span>记录日期</span>
              <input
                type="date"
                className="field"
                value={form.date}
                onChange={(event) => setForm((current) => ({ ...current, date: event.target.value }))}
              />
            </label>
            <label className="space-y-2 text-sm text-slate-500">
              <span>标签分类</span>
              <input
                className="field"
                placeholder="第一次, 出游, 生病"
                value={form.tags}
                onChange={(event) => setForm((current) => ({ ...current, tags: event.target.value }))}
              />
            </label>
          </div>
          <label className="block space-y-2 text-sm text-slate-500">
            <span>上传照片</span>
            <input
              type="file"
              className="field cursor-pointer"
              multiple
              accept="image/*"
              onChange={(event) => setFiles(Array.from(event.target.files || []))}
            />
          </label>
          {files.length > 0 ? (
            <div className="flex flex-wrap gap-2">
              {files.map((file) => (
                <span key={`${file.name}-${file.size}`} className="pill">
                  {file.name}
                </span>
              ))}
            </div>
          ) : null}
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={submitting}
              className="rounded-2xl bg-slate-900 px-5 py-3 text-sm font-medium text-white transition hover:bg-slate-700 disabled:cursor-not-allowed disabled:bg-slate-300"
            >
              {submitting ? '保存中...' : '发布记录'}
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}

function Timeline({ records }) {
  return (
    <section className="space-y-5">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-medium text-slate-900">成长时光轴</h2>
          <p className="mt-2 text-sm text-slate-500">按时间倒序展示，记录每个瞬间对应的精确月龄。</p>
        </div>
        <span className="pill">{records.length} 条记录</span>
      </div>

      {records.length === 0 ? (
        <div className="surface p-10 text-center text-sm leading-7 text-slate-500">
          还没有成长记录。登录后台后，发布第一篇图文记录吧。
        </div>
      ) : (
        <div className="space-y-5">
          {records.map((record) => (
            <TimelineItem key={record.id} record={record} />
          ))}
        </div>
      )}
    </section>
  )
}

function TimelineItem({ record }) {
  const normalizedContent = record.content.replaceAll(/[#>*_`\n\r]+/g, ' ').replaceAll(/\s+/g, ' ').trim()
  const hasMoreContent = record.excerpt !== normalizedContent

  return (
    <article className="surface overflow-hidden">
      <div className="grid gap-0 lg:grid-cols-[112px_1fr]">
        <div className="border-b border-slate-100 bg-slate-50/70 px-6 py-6 lg:border-b-0 lg:border-r">
          <div className="text-xs uppercase tracking-[0.24em] text-slate-400">Time</div>
          <div className="mt-3 text-sm font-medium text-slate-900">{record.date}</div>
          <div className="mt-2 text-sm text-slate-500">{record.ageLabel}</div>
        </div>
        <div className="space-y-5 p-6 sm:p-7">
          <div className="flex flex-wrap items-center gap-2">
            {record.tags.length > 0 ? (
              record.tags.map((tag) => (
                <span key={tag} className="pill">
                  #{tag}
                </span>
              ))
            ) : (
              <span className="pill">日常记录</span>
            )}
          </div>

          {record.photos.length > 0 ? (
            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              {record.photos.map((photo) => (
                <a key={photo.url} href={photo.url} target="_blank" rel="noreferrer" className="overflow-hidden rounded-3xl border border-slate-200 bg-slate-50">
                  <img src={photo.url} alt={photo.fileName} className="aspect-[4/3] h-full w-full object-cover transition hover:scale-[1.02]" />
                </a>
              ))}
            </div>
          ) : null}

          <div className="rounded-3xl border border-slate-100 bg-white p-5">
            <p className="text-sm leading-7 text-slate-600">{record.excerpt || normalizedContent || '这条记录没有可展示的摘要。'}</p>
            {hasMoreContent ? (
              <details className="mt-4">
                <summary className="cursor-pointer text-sm font-medium text-slate-500">展开全文</summary>
                <div className="prose prose-slate mt-4 max-w-none text-sm leading-7">
                  <ReactMarkdown>{record.content}</ReactMarkdown>
                </div>
              </details>
            ) : null}
          </div>
        </div>
      </div>
    </article>
  )
}

function App() {
  const [home, setHome] = useState(null)
  const [settings, setSettings] = useState(null)
  const [auth, setAuth] = useState({ authenticated: false, username: null })
  const [loginForm, setLoginForm] = useState({ username: '', password: '' })
  const [recordForm, setRecordForm] = useState(initialRecordForm)
  const [selectedFiles, setSelectedFiles] = useState([])
  const [busy, setBusy] = useState(true)
  const [loginBusy, setLoginBusy] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  const records = useMemo(() => home?.records || [], [home])

  const loadHome = async () => {
    const data = await apiFetch('/api/public/home')
    setHome(data)
  }

  const loadAuth = async () => {
    const status = await apiFetch('/api/auth/me')
    setAuth(status)
    if (status.authenticated) {
      const adminSettings = await apiFetch('/api/admin/settings')
      setSettings(adminSettings)
    } else {
      setSettings(null)
    }
  }

  const refreshData = async () => {
    setBusy(true)
    setError('')
    try {
      await Promise.all([loadHome(), loadAuth()])
    } catch (bootstrapError) {
      setError(bootstrapError.message)
    } finally {
      setBusy(false)
    }
  }

  useEffect(() => {
    const initialize = async () => {
      setError('')
      try {
        await Promise.all([loadHome(), loadAuth()])
      } catch (bootstrapError) {
        setError(bootstrapError.message)
      } finally {
        setBusy(false)
      }
    }

    initialize()
  }, [])

  const handleLogin = async (event) => {
    event.preventDefault()
    setLoginBusy(true)
    setMessage('')
    setError('')
    try {
      const status = await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify(loginForm),
      })
      setAuth(status)
      const adminSettings = await apiFetch('/api/admin/settings')
      setSettings(adminSettings)
      setLoginForm({ username: '', password: '' })
      setMessage('登录成功，可以开始记录成长瞬间了。')
    } catch (loginError) {
      setError(loginError.message)
    } finally {
      setLoginBusy(false)
    }
  }

  const handleLogout = async () => {
    setError('')
    setMessage('')
    try {
      await apiFetch('/api/auth/logout', { method: 'POST' })
      setAuth({ authenticated: false, username: null })
      setSettings(null)
      setMessage('已退出后台登录。')
    } catch (logoutError) {
      setError(logoutError.message)
    }
  }

  const handleCreateRecord = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    setMessage('')
    try {
      const formData = new FormData()
      formData.append('content', recordForm.content)
      formData.append('date', recordForm.date)
      formData.append('tags', recordForm.tags)
      selectedFiles.forEach((file) => formData.append('files', file))
      await apiFetch('/api/admin/records', {
        method: 'POST',
        body: formData,
      })
      setRecordForm(initialRecordForm)
      setSelectedFiles([])
      setMessage('记录已发布，时间轴已更新。')
      await loadHome()
    } catch (submitError) {
      setError(submitError.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (busy) {
    return (
      <div className="flex min-h-screen items-center justify-center bg-slate-50 px-6 text-sm text-slate-500">
        正在准备 Baby Steps...
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800">
      <main className="mx-auto flex w-full max-w-6xl flex-col gap-6 px-4 py-6 sm:px-6 sm:py-8 lg:px-8 lg:py-10">
        <Header home={home} auth={auth} onRefresh={refreshData} onLogout={handleLogout} />
        <LoginCard
          auth={auth}
          loginForm={loginForm}
          setLoginForm={setLoginForm}
          onLogin={handleLogin}
          loginBusy={loginBusy}
        />
        {auth.authenticated ? (
          <AdminPanel
            settings={settings}
            form={recordForm}
            setForm={setRecordForm}
            files={selectedFiles}
            setFiles={setSelectedFiles}
            submitting={submitting}
            onSubmit={handleCreateRecord}
          />
        ) : null}

        {(message || error) && (
          <div
            className={`rounded-2xl border px-4 py-3 text-sm ${
              error
                ? 'border-rose-200 bg-rose-50 text-rose-700'
                : 'border-emerald-200 bg-emerald-50 text-emerald-700'
            }`}
          >
            {error || message}
          </div>
        )}

        <Timeline records={records} />
      </main>
    </div>
  )
}

export default App
