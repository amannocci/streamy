module.exports = {
  title: 'Streamy',
  description: 'Build powerful reactive, concurrent, and distributed applications stream applications.',
  base: '/streamy/',
  themeConfig: {
    sidebarDepth: 2,
    nav: [
      { text: 'Home', link: '/' },
      { text: 'About', link: '/about/' },
      { text: 'Guide', link: '/guide/' },
      { text: 'Develop', link: '/develop/' },
    ],
    sidebar: {
      '/about/': [
        '',
        'concept'
      ],
      '/guide/': [
        '',
        'setup',
        'administration'
      ],
      '/develop/': [
        '',
        'create-a-plugin',
        'use-a-plugin',
        'recipe',
        'integration'
      ]
    }
  }
}
