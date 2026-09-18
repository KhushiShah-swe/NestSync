import { test, expect } from '@playwright/test'
test('a roommate can register, track a bill, complete a chore, and sign out', async ({ page }) => {
  const stamp = Date.now()
  await page.goto('/register')
  await page.getByLabel('Your name').fill('Taylor')
  await page.getByLabel('Email address').fill(`taylor-${stamp}@example.com`)
  await page.getByLabel('Password', { exact: true }).fill('demo-password-123')
  const registration = page.waitForResponse(
    (response) =>
      response.url().endsWith('/api/auth/register') && response.request().method() === 'POST',
  )
  await page.getByRole('button', { name: 'Create account' }).click()
  expect((await registration).status()).toBe(201)
  await expect(page.getByRole('heading', { name: 'A little more in sync, Taylor.' })).toBeVisible()
  await page.getByRole('link', { name: '+ Add expense' }).click()
  await page.getByLabel('Expense title').fill('Internet bill')
  await page.getByLabel('Amount (USD)').fill('60.00')
  await page.getByRole('button', { name: 'Save expense' }).click()
  await expect(page.getByRole('heading', { name: 'Internet bill' })).toBeVisible()
  await page.getByRole('link', { name: 'Chores', exact: true }).click()
  await page.getByRole('link', { name: '+ Add chore' }).click()
  await page.getByLabel('Chore name').fill('Clean kitchen')
  await page.getByRole('button', { name: 'Save chore' }).click()
  await page.getByRole('button', { name: 'Mark done' }).click()
  await expect(page.getByRole('button', { name: 'Reopen' })).toBeVisible()
  await page.getByRole('button', { name: 'Sign out' }).click()
  await expect(page.getByRole('heading', { name: 'Welcome home.' })).toBeVisible()
  await page.goto('/expenses')
  await expect(page).toHaveURL(/login/)
})
test('the mobile registration layout has no horizontal overflow', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/register')
  await expect(page.getByRole('button', { name: 'Create account' })).toBeVisible()
  expect(
    await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth),
  ).toBeTruthy()
})
