import { Routes } from '@angular/router';
import { VitrineComponent } from './pages/vitrine/vitrine';
import { LoginComponent } from './pages/login/login';
import { VenderComponent } from './pages/vender/vender';
import { CadastroComponent } from './pages/cadastro/cadastro';
import { MeusAnunciosComponent } from './pages/meus-anuncios/meus-anuncios';
import { ChatsListComponent } from './pages/chats-list/chats-list';
import { ChatConversaComponent } from './pages/chat-conversa/chat-conversa';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'vitrine', pathMatch: 'full' },
  { path: 'vitrine', component: VitrineComponent },
  { path: 'login', component: LoginComponent },
  { path: 'vender', component: VenderComponent, canActivate: [authGuard] },
  { path: 'cadastro', component: CadastroComponent },
  { path: 'meus-anuncios', component: MeusAnunciosComponent, canActivate: [authGuard] },
  { path: 'chats', component: ChatsListComponent, canActivate: [authGuard] },
  { path: 'chats/:id', component: ChatConversaComponent, canActivate: [authGuard] },
];
