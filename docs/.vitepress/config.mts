import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'MistTags',
  description: 'Lightweight prefix/suffix tags for Minecraft servers.',
  base: '/MistTags/',
  cleanUrls: false,
  lastUpdated: true,
  themeConfig: {
    logo: '/logo.svg',
    nav: [
      { text: 'Guide', link: '/guide/' },
      { text: 'Commands', link: '/guide/commands' },
      { text: 'Animations', link: '/guide/animations' },
      { text: 'Groups', link: '/guide/groups' },
      { text: 'Placeholders', link: '/guide/placeholders' },
      { text: 'Config', link: '/guide/config' },
      { text: 'GitHub', link: 'https://github.com/MistDig/MistTags' }
    ],
    sidebar: [
      {
        text: 'User Guide',
        items: [
          { text: 'Installation', link: '/guide/' },
          { text: 'Commands & Permissions', link: '/guide/commands' },
          { text: 'Animation Presets', link: '/guide/animations' },
          { text: 'Groups', link: '/guide/groups' },
          { text: 'Placeholders', link: '/guide/placeholders' },
          { text: 'Configuration', link: '/guide/config' },
          { text: 'Integrations', link: '/guide/integrations' },
          { text: 'FAQ', link: '/guide/faq' }
        ]
      },
      {
        text: 'Project',
        items: [
          { text: 'Changelog', link: '/changelog' }
        ]
      }
    ],
    search: {
      provider: 'local'
    },
    socialLinks: [
      { icon: 'github', link: 'https://github.com/MistDig/MistTags' }
    ],
    footer: {
      message: 'Released as a beta build. Test on your server before using in production.',
      copyright: 'Copyright (c) 2026 MistDig'
    }
  }
})
