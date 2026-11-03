import { useEffect, useRef, useState } from 'react'

interface SmartCaptchaWidgetProps {
  visible: boolean
  resetKey: number
  onToken: (token: string) => void
  onError: (message: string) => void
}

interface SmartCaptchaApi {
  render: (
    container: HTMLElement,
    options: {
      sitekey: string
      callback: (token: string) => void
      'expired-callback'?: () => void
      'error-callback'?: () => void
    }
  ) => number
  reset: (widgetId: number) => void
}

declare global {
  interface Window {
    smartCaptcha?: SmartCaptchaApi
  }
}

const SCRIPT_ID = 'yandex-smartcaptcha-script'
const SCRIPT_SRC = 'https://smartcaptcha.yandexcloud.net/captcha.js'

export default function SmartCaptchaWidget({
  visible,
  resetKey,
  onToken,
  onError,
}: SmartCaptchaWidgetProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const widgetIdRef = useRef<number | null>(null)
  const missingKeyReportedRef = useRef(false)
  const [ready, setReady] = useState(Boolean(window.smartCaptcha))
  const clientKey = import.meta.env.VITE_SMARTCAPTCHA_CLIENT_KEY as string | undefined

  useEffect(() => {
    if (!visible || ready) {
      return
    }

    if (!clientKey) {
      if (!missingKeyReportedRef.current) {
        missingKeyReportedRef.current = true
        onError('SmartCaptcha Client Key не настроен в окружении frontend.')
      }
      return
    }

    missingKeyReportedRef.current = false

    const existingScript = document.getElementById(SCRIPT_ID) as HTMLScriptElement | null
    const script = existingScript ?? document.createElement('script')
    script.id = SCRIPT_ID
    script.src = SCRIPT_SRC
    script.async = true
    script.onload = () => setReady(true)
    script.onerror = () => onError('Не удалось загрузить SmartCaptcha.')
    if (!existingScript) {
      document.head.appendChild(script)
    }
  }, [clientKey, onError, ready, visible])

  useEffect(() => {
    if (!visible || !ready || !clientKey || !containerRef.current || !window.smartCaptcha) {
      return
    }

    containerRef.current.replaceChildren()
    widgetIdRef.current = window.smartCaptcha.render(containerRef.current, {
      sitekey: clientKey,
      callback: onToken,
      'expired-callback': () => onError('Токен SmartCaptcha истёк. Пройдите проверку ещё раз.'),
      'error-callback': () => onError('SmartCaptcha вернула ошибку. Пройдите проверку ещё раз.'),
    })

    return () => {
      if (widgetIdRef.current !== null && window.smartCaptcha) {
        window.smartCaptcha.reset(widgetIdRef.current)
      }
      widgetIdRef.current = null
    }
  }, [clientKey, onError, onToken, ready, resetKey, visible])

  if (!visible) {
    return null
  }

  return <div ref={containerRef} className="smartcaptcha-widget" aria-live="polite" />
}
