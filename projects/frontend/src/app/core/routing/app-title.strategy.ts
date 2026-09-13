import { inject, Service } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterStateSnapshot, TitleStrategy } from '@angular/router';

export const APP_NAME = 'Portal Rick y Morty';

/** Route `title` values become "<title> · Portal Rick y Morty"; pages can override at runtime. */
@Service({ autoProvided: false })
export class AppTitleStrategy extends TitleStrategy {
  private readonly title = inject(Title);

  override updateTitle(snapshot: RouterStateSnapshot): void {
    const routeTitle = this.buildTitle(snapshot);
    this.title.setTitle(routeTitle ? `${routeTitle} · ${APP_NAME}` : APP_NAME);
  }
}
