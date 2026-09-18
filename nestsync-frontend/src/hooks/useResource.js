import { useEffect, useState } from 'react'
import API, { errorMessage } from '../api/axiosConfig'
export default function useResource(path) {
  const [result, setResult] = useState({ data: null, loading: true, error: '' })
  const [revision, setRevision] = useState(0)
  useEffect(() => {
    const controller = new AbortController()
    API.get(path, { signal: controller.signal })
      .then(({ data }) => {
        setResult({ data, loading: false, error: '' })
      })
      .catch((error) => {
        if (!controller.signal.aborted)
          setResult({ data: null, loading: false, error: errorMessage(error) })
      })
    return () => controller.abort()
  }, [path, revision])
  return { ...result, reload: () => setRevision((value) => value + 1) }
}
